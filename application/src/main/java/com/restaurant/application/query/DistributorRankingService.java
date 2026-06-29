package com.restaurant.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistributorRankingService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Blended logic: Gets the most recent price (Offer vs Purchase) per distributor per product,
     * and ranks them ascending (cheapest first).
     */
    private static final String RANKING_CTE = """
        WITH combined_prices AS (
            -- 1. Official Offers
            SELECT 
                primary_product_id,
                distributor_id,
                price_cents as unit_price_cents,
                valid_from as last_updated,
                'OFFER' as source
            FROM distributor_offers
            
            UNION ALL
            
            -- 2. Actual Purchases
            SELECT 
                pl.primary_product_id,
                p.distributor_id,
                pl.unit_price_cents,
                p.purchase_date as last_updated,
                'PURCHASE' as source
            FROM purchase_lines pl
            JOIN purchases p ON pl.purchase_id = p.id
        ),
        ranked_by_recency AS (
            SELECT 
                primary_product_id,
                distributor_id,
                unit_price_cents,
                last_updated,
                source,
                ROW_NUMBER() OVER(PARTITION BY primary_product_id, distributor_id ORDER BY last_updated DESC) as rn
            FROM combined_prices
        ),
        latest_prices AS (
            SELECT * FROM ranked_by_recency WHERE rn = 1
        )
        """;

    public List<Map<String, Object>> getBestDistributorPerProduct(boolean lowStockOnly, List<String> productIds) {
        // Ranks distributors per product, returning ONLY the #1 best (cheapest) for each product
        // Includes ORPHANED products (LEFT JOIN from primary_products)
        String sql = RANKING_CTE + """
            , ranked_by_price AS (
                SELECT 
                    pp.id as product_id,
                    pp.name as product_name,
                    pp.category as category,
                    pp.current_stock as current_stock,
                    pp.minimum_stock_alert as min_stock,
                    d.id as distributor_id,
                    d.name as distributor_name,
                    lp.unit_price_cents,
                    lp.last_updated,
                    lp.source,
                    ROW_NUMBER() OVER(PARTITION BY pp.id ORDER BY lp.unit_price_cents ASC) as price_rank
                FROM primary_products pp
                LEFT JOIN latest_prices lp ON pp.id = lp.primary_product_id
                LEFT JOIN distributors d ON lp.distributor_id = d.id
            )
            SELECT * FROM ranked_by_price WHERE price_rank = 1
        """;
        
        StringBuilder whereClause = new StringBuilder(" AND (1=1");
        if (lowStockOnly) {
            whereClause.append(" AND current_stock < min_stock");
        }
        if (productIds != null && !productIds.isEmpty()) {
            whereClause.append(" AND product_id IN (");
            for (int i = 0; i < productIds.size(); i++) {
                whereClause.append("'").append(productIds.get(i).replace("'", "''")).append("'");
                if (i < productIds.size() - 1) whereClause.append(",");
            }
            whereClause.append(")");
        }
        whereClause.append(")");
        
        sql += whereClause.toString() + " ORDER BY category ASC, product_name ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String distId = rs.getString("distributor_id");
            return Map.of(
                    "productId", rs.getString("product_id"),
                    "productName", rs.getString("product_name"),
                    "category", rs.getString("category") != null ? rs.getString("category") : "Sin Categoría",
                    "distributorId", distId != null ? distId : "NONE",
                    "distributorName", distId != null ? rs.getString("distributor_name") : "Sin proveedor registrado",
                    "costPerGram", distId != null ? rs.getLong("unit_price_cents") / 100.0 : 0.0,
                    "lastUpdated", distId != null && rs.getTimestamp("last_updated") != null ? rs.getTimestamp("last_updated").toLocalDateTime().toString() : "N/A",
                    "source", distId != null ? rs.getString("source") : "N/A",
                    "currentStock", rs.getLong("current_stock"),
                    "minStock", rs.getLong("min_stock")
            );
        });
    }

    /**
     * Calculates total potential savings if we had bought everything at the BEST known price 
     * over the last 30 days, compared to what we actually paid.
     */
    public Map<String, Object> calculatePotentialSavingsLast30Days() {
        // 1. Get actual volumes and average prices paid over last 30 days
        String actualsSql = """
            SELECT 
                pl.primary_product_id,
                SUM(pl.quantity) as total_qty,
                SUM(pl.unit_price_cents * pl.quantity) as total_spent
            FROM purchase_lines pl
            JOIN purchases p ON pl.purchase_id = p.id
            WHERE p.purchase_date >= current_date - interval '30 days'
            GROUP BY pl.primary_product_id
        """;
        
        List<Map<String, Object>> actuals = jdbcTemplate.queryForList(actualsSql);
        
        // 2. Get best prices right now (for ALL products to calculate savings against reality)
        List<Map<String, Object>> bestPrices = getBestDistributorPerProduct(false, null);
        
        long totalSpentCents = 0;
        long optimizedCostCents = 0;
        
        for (Map<String, Object> actual : actuals) {
            String pId = actual.get("primary_product_id").toString();
            long qty = ((Number) actual.get("total_qty")).longValue();
            long spent = ((Number) actual.get("total_spent")).longValue();
            
            totalSpentCents += spent;
            
            // Find best price
            Map<String, Object> bestMatch = bestPrices.stream()
                .filter(b -> b.get("productId").toString().equals(pId))
                .findFirst()
                .orElse(null);
                
            if (bestMatch != null && !"NONE".equals(bestMatch.get("distributorId"))) {
                double bestPrice = ((Number) bestMatch.get("costPerGram")).doubleValue();
                optimizedCostCents += (long) (bestPrice * 100 * qty);
            } else {
                optimizedCostCents += spent; // No alternative, cost remains the same
            }
        }
        
        long potentialSavingsCents = totalSpentCents - optimizedCostCents;
        
        return Map.of(
            "actualSpent", totalSpentCents / 100.0,
            "optimizedCost", optimizedCostCents / 100.0,
            "potentialSavings", potentialSavingsCents > 0 ? potentialSavingsCents / 100.0 : 0.0
        );
    }

    public List<Map<String, Object>> getRankedDistributorsForProduct(String productId) {
        // Returns the full podium for a specific product
        String sql = RANKING_CTE + """
            SELECT 
                d.id as distributor_id,
                d.name as distributor_name,
                lp.unit_price_cents,
                lp.last_updated,
                lp.source
            FROM latest_prices lp
            JOIN distributors d ON lp.distributor_id = d.id
            WHERE lp.primary_product_id = ?::uuid
            ORDER BY lp.unit_price_cents ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "rank", rowNum + 1,
                "distributorId", rs.getString("distributor_id"),
                "distributorName", rs.getString("distributor_name"),
                "costPerGram", rs.getLong("unit_price_cents") / 100.0,
                "lastUpdated", rs.getTimestamp("last_updated").toLocalDateTime().toString(),
                "source", rs.getString("source")
        ), productId);
    }
}
