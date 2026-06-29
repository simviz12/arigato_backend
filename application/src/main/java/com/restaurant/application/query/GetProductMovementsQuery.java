package com.restaurant.application.query;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProductMovementsQuery {

    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    public static class MovementDto {
        private UUID id;
        private String movementType;
        private BigDecimal quantity;
        private LocalDateTime timestamp;
        private UUID referenceId;
    }

    public List<MovementDto> execute(UUID productId) {
        String sql = """
            SELECT id, movement_type, quantity, created_at, reference_id
            FROM inventory_movements
            WHERE product_id = ?
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> MovementDto.builder()
                .id((UUID) rs.getObject("id"))
                .movementType(rs.getString("movement_type"))
                .quantity(rs.getBigDecimal("quantity"))
                .timestamp(rs.getTimestamp("created_at").toLocalDateTime())
                .referenceId((UUID) rs.getObject("reference_id"))
                .build(), productId);
    }
}
