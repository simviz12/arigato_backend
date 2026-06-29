package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.vo.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ConcurrentSalesStressTest {

    @Autowired
    private ProcessSaleRetryWrapper retryWrapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void twentyThreadsCompetingForTenItems() throws InterruptedException {
        // Setup: A Primary Product (Coca-Cola) with exactly 10 units in stock
        UUID cokeId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO primary_products (id, name, unit_of_measure, current_stock_quantity, created_at, updated_at, version) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                cokeId, "Coca-Cola", "UNIT", 10.0000);
                
        // Setup: A Final Product that just sells 1 unit of Coca-Cola
        UUID productId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO final_products (id, name, selling_price_cents, category, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                productId, "Coca-Cola Helada", 300000, "GENERAL", true);
                
        jdbcTemplate.update("INSERT INTO final_product_components (final_product_id, primary_product_id, quantity_grams) VALUES (?, ?, ?)",
                productId, cokeId, 1.0000);

        // We simulate 20 cashiers clicking "Sell" at the exact same millisecond
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1); // To make all threads start exactly at the same time
        CountDownLatch done = new CountDownLatch(threadCount); // To wait for all to finish

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ProcessSaleUseCase.Command cmd = ProcessSaleUseCase.Command.builder()
                .cashierId("Cashier")
                .paymentMethod("CASH")
                .lines(java.util.List.of(
                        ProcessSaleUseCase.LineItemDto.builder().finalProductId(productId).quantity(1).build()
                ))
                .build();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // wait for the starting gun
                    retryWrapper.executeWithRetry(cmd);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown(); // FIRE!
        done.await(); // Wait for all threads to finish

        // Assert exactly 10 succeeded and 10 failed
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(10);

        // Assert stock is exactly 0 and NOT negative
        Double remainingStock = jdbcTemplate.queryForObject("SELECT current_stock_quantity FROM primary_products WHERE id = ?", Double.class, cokeId);
        assertThat(remainingStock).isEqualTo(0.0);
    }
}
