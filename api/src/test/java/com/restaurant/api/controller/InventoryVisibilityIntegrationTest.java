package com.restaurant.api.controller;

import com.restaurant.application.query.GetDailyInventorySnapshotQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryVisibilityIntegrationTest {

    @Autowired
    private InventoryQueryController controller;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void dailySnapshotReturnsAccurateFiguresInUnder300ms() {
        // 1. Setup Data
        UUID finalProductId = UUID.randomUUID();
        
        jdbcTemplate.update("INSERT INTO final_products (id, name, selling_price_cents, category, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                finalProductId, "Speed Test Dish", 15000, "GENERAL", true);

        // Inject 1000 sales to stress the CQRS aggregation
        for (int i = 0; i < 1000; i++) {
            UUID saleId = UUID.randomUUID();
            jdbcTemplate.update("INSERT INTO sales (id, cashier_id, sale_date, payment_method, total_amount_cents, discount_cents, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'CASH', 15000, 0, 'COMPLETED')",
                    saleId, "TestCashier");
                    
            jdbcTemplate.update("INSERT INTO sale_items (id, sale_id, final_product_id, quantity) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(), saleId, finalProductId, 1);
        }

        // 2. Execute and Time the Query
        long startTime = System.currentTimeMillis();
        ResponseEntity<List<GetDailyInventorySnapshotQuery.SnapshotDto>> response = controller.getDailySnapshot();
        long executionTimeMs = System.currentTimeMillis() - startTime;

        // 3. Assert Results and SLA
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        // Find our specific test dish in the snapshot
        GetDailyInventorySnapshotQuery.SnapshotDto testDish = response.getBody().stream()
                .filter(dto -> dto.getProductId().equals(finalProductId))
                .findFirst()
                .orElseThrow();

        assertThat(testDish.getTotalSoldToday().intValue()).isEqualTo(1000);
        assertThat(testDish.getTotalSoldTodayRevenue().intValue()).isEqualTo(15000 * 1000 / 100); // 15000 cents * 1000 = 150,000 pesos

        // Assert SLA < 300ms (It usually takes < 50ms with indexes)
        assertThat(executionTimeMs).isLessThan(300L);
    }
}
