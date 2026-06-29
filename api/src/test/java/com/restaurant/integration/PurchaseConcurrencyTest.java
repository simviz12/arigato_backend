package com.restaurant.integration;

import com.restaurant.application.dto.command.RegisterPurchaseCommand;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.PrimaryProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class PurchaseConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PrimaryProductRepository productRepository;

    private UUID productId;
    private UUID distributorId;
    private String token;

    @BeforeEach
    void setUp() {
        // Assume an auth token is obtained (for simplicity, bypassed or mocked in this test depending on SecurityConfig)
        productId = UUID.randomUUID();
        distributorId = UUID.randomUUID();

        PrimaryProduct product = PrimaryProduct.builder()
                .id(productId)
                .name("Flour")
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .currentStock(Weight.ofGrams(1000))
                .build();
        productRepository.save(product);
    }

    @Test
    void testConcurrentPurchasesDoNotLoseUpdates() throws InterruptedException {
        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        RegisterPurchaseCommand command = new RegisterPurchaseCommand();
        command.setProductId(productId);
        command.setDistributorId(distributorId);
        command.setQuantityGrams(100.0); // 100g per purchase
        command.setTotalPricePesos(200.0);

        String url = "http://localhost:" + port + "/api/purchases";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Runnable task = () -> {
            try {
                restTemplate.postForEntity(url, new HttpEntity<>(command, headers), Void.class);
            } finally {
                latch.countDown();
            }
        };

        // Execute 50 requests concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(task);
        }

        latch.await();

        // Validate lost update did not occur. Initial: 1000g. Added: 50 * 100g = 5000g. Expected: 6000g.
        PrimaryProduct productAfter = productRepository.findById(productId).orElseThrow();
        assertThat(productAfter.getCurrentStock().getGrams()).isEqualByComparingTo(new java.math.BigDecimal("6000"));
    }
}
