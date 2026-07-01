package com.restaurant.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Devuelve el resumen global para un periodo.
     * Basado estrictamente en las reglas de /docs/glossary.md
     */
    public Map<String, Object> getSummary(LocalDateTime from, LocalDateTime to) {
        
        // 1. INGRESOS (Revenue)
        // Sum of sale totals
        String sqlIngresos = "SELECT COALESCE(SUM(total_amount_cents), 0)::bigint FROM sales WHERE sale_date >= ? AND sale_date <= ?";
        Long ingresosCents = jdbcTemplate.queryForObject(sqlIngresos, Long.class, from, to);

        // 2. COSTO (COGS)
        // Sum of (unit_cost_cents * quantity) from sale_items
        String sqlCosto = """
                SELECT COALESCE(SUM(sl.unit_cost_cents * sl.quantity), 0)::bigint 
                FROM sale_items sl
                JOIN sales s ON sl.sale_id = s.id
                WHERE s.sale_date >= ? AND s.sale_date <= ?
                """;
        Long costoCents = jdbcTemplate.queryForObject(sqlCosto, Long.class, from, to);

        // 3. GASTO (Purchases)
        // Sum of purchases (inventory bought)
        String sqlGasto = "SELECT COALESCE(SUM(total_price_cents), 0)::bigint FROM purchases WHERE purchase_date >= ? AND purchase_date <= ?";
        Long gastoCents = jdbcTemplate.queryForObject(sqlGasto, Long.class, from, to);

        // 4. RENTABILIDAD
        Long rentabilidadCents = ingresosCents - costoCents;

        return Map.of(
                "ingresos", ingresosCents / 100.0,
                "costo", costoCents / 100.0,
                "gasto", gastoCents / 100.0,
                "rentabilidad", rentabilidadCents / 100.0
        );
    }

    /**
     * Devuelve la serie de tiempo agrupada por granularidad para gráficos.
     */
    public List<Map<String, Object>> getTimeSeries(String granularity, LocalDateTime from, LocalDateTime to) {
        
        // Postgres DATE_TRUNC mappings
        String dateTruncFormat = switch (granularity.toLowerCase()) {
            case "year" -> "year";
            case "month" -> "month";
            case "week" -> "week";
            default -> "day";
        };

        // CTE for Sales (Ingresos & Costo) bucketed by date
        String sql = """
            WITH sales_bucket AS (
                SELECT 
                    DATE_TRUNC('%s', s.sale_date) as bucket_date,
                    COALESCE(SUM(s.total_amount_cents), 0)::bigint as ingresos,
                    COALESCE(SUM(sl.unit_cost_cents * sl.quantity), 0)::bigint as costo
                FROM sales s
                LEFT JOIN sale_items sl ON s.id = sl.sale_id
                WHERE s.sale_date >= ? AND s.sale_date <= ?
                GROUP BY DATE_TRUNC('%s', s.sale_date)
            ),
            purchases_bucket AS (
                SELECT 
                    DATE_TRUNC('%s', purchase_date) as bucket_date,
                    COALESCE(SUM(total_price_cents), 0)::bigint as gasto
                FROM purchases
                WHERE purchase_date >= ? AND purchase_date <= ?
                GROUP BY DATE_TRUNC('%s', purchase_date)
            )
            SELECT 
                COALESCE(s.bucket_date, p.bucket_date) as time_bucket,
                COALESCE(s.ingresos, 0) as ingresos,
                COALESCE(s.costo, 0) as costo,
                COALESCE(p.gasto, 0) as gasto
            FROM sales_bucket s
            FULL OUTER JOIN purchases_bucket p ON s.bucket_date = p.bucket_date
            ORDER BY time_bucket ASC
        """.formatted(dateTruncFormat, dateTruncFormat, dateTruncFormat, dateTruncFormat);

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "date", rs.getTimestamp("time_bucket").toLocalDateTime().toString(),
                "ingresos", rs.getLong("ingresos") / 100.0,
                "costo", rs.getLong("costo") / 100.0,
                "gasto", rs.getLong("gasto") / 100.0,
                "rentabilidad", (rs.getLong("ingresos") - rs.getLong("costo")) / 100.0
        ), from, to, from, to);
    }

    /**
     * Devuelve la comparacion contra el periodo anterior.
     */
    public Map<String, Object> getComparison(String period, LocalDateTime referenceDate, String comparisonMode) {
        LocalDateTime currentStart;
        LocalDateTime currentEnd = referenceDate;
        LocalDateTime priorStart;
        LocalDateTime priorEnd;

        if ("week".equalsIgnoreCase(period)) {
            // Assume week starts Monday. Simple subtraction for demonstration.
            currentStart = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
            priorStart = currentStart.minusWeeks(1);
            if ("partial".equalsIgnoreCase(comparisonMode)) {
                priorEnd = priorStart.plusDays(referenceDate.getDayOfWeek().getValue() - 1).withHour(referenceDate.getHour()).withMinute(referenceDate.getMinute());
            } else {
                priorEnd = priorStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
            }
        } else {
            // Month
            currentStart = referenceDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            priorStart = currentStart.minusMonths(1);
            if ("partial".equalsIgnoreCase(comparisonMode)) {
                // Apples to apples: e.g. up to 15th of prior month, taking care of month length bounds
                int targetDay = Math.min(referenceDate.getDayOfMonth(), priorStart.toLocalDate().lengthOfMonth());
                priorEnd = priorStart.withDayOfMonth(targetDay).withHour(referenceDate.getHour()).withMinute(referenceDate.getMinute());
            } else {
                priorEnd = priorStart.withDayOfMonth(priorStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            }
        }

        Map<String, Object> current = getSummary(currentStart, currentEnd);
        Map<String, Object> prior = getSummary(priorStart, priorEnd);

        return Map.of(
                "currentPeriod", current,
                "priorPeriod", prior,
                "deltas", Map.of(
                        "ingresos", calculateDelta(current.get("ingresos"), prior.get("ingresos")),
                        "costo", calculateDelta(current.get("costo"), prior.get("costo")),
                        "gasto", calculateDelta(current.get("gasto"), prior.get("gasto")),
                        "rentabilidad", calculateDelta(current.get("rentabilidad"), prior.get("rentabilidad"))
                ),
                "metadata", Map.of(
                        "currentRange", currentStart.toString() + " to " + currentEnd.toString(),
                        "priorRange", priorStart.toString() + " to " + priorEnd.toString(),
                        "mode", comparisonMode
                )
        );
    }

    private Map<String, Object> calculateDelta(Object currentObj, Object priorObj) {
        double current = ((Number) currentObj).doubleValue();
        double prior = ((Number) priorObj).doubleValue();
        
        double absolute = current - prior;
        Double percentage = null;
        
        if (prior > 0) {
            percentage = (absolute / prior) * 100.0;
        } else if (prior < 0) {
            percentage = (absolute / Math.abs(prior)) * 100.0;
        } else if (current > 0) {
            // Prior is 0, current > 0 -> 100% growth essentially (or conceptually infinite). 
            // Returning null or 100 is a business decision. We use null for 'N/A' clean UI display.
            percentage = null;
        }

        return Map.of(
                "absolute", absolute,
                "percentage", percentage == null ? "N/A" : Math.round(percentage * 10.0) / 10.0
        );
    }

    /**
     * Rastreo historico de precio de compra de un ingrediente.
     */
    public List<Map<String, Object>> getPriceTrends(String primaryProductId) {
        String sql = """
            SELECT 
                purchase_date as date,
                (total_price_cents / quantity)::bigint as unit_price_cents
            FROM purchases
            WHERE primary_product_id = ?::uuid
            ORDER BY purchase_date ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "date", rs.getTimestamp("date").toLocalDateTime().toString(),
                "costPerGram", rs.getLong("unit_price_cents") / 100.0 // Assumes unit is always consistent
        ), primaryProductId);
    }
}
