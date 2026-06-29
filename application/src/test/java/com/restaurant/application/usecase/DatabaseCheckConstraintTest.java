package com.restaurant.application.usecase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DatabaseCheckConstraintTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void databasePhysicallyRejectsNegativeStockBypassingJava() {
        // Setup: Product with 5 units of stock
        UUID dummyId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO primary_products (id, name, unit_of_measure, current_stock_quantity, created_at, updated_at, version) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                dummyId, "Hacker Product", "UNIT", 5.0000);

        // Attempt SQL injection / direct bug bypass setting stock to -1
        // The V5 migration CHECK (current_stock_quantity >= 0) must abort this at the PostgreSQL engine level
        assertThatThrownBy(() -> 
            jdbcTemplate.update("UPDATE primary_products SET current_stock_quantity = -1 WHERE id = ?", dummyId)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
