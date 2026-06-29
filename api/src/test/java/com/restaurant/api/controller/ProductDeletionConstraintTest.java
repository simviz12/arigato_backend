package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateFinalProductUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductDeletionConstraintTest {

    @Autowired
    private FinalProductController controller;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cannotHardDeleteProductReferencedBySale() {
        // 1. Create a product via usecase
        CreateFinalProductUseCase.Command cmd = new CreateFinalProductUseCase.Command();
        cmd.setName("Test Product");
        cmd.setSellingPricePesos(new BigDecimal("100"));
        cmd.setCategory("GENERAL");
        
        // Note: For this pure DB constraint test, we'll manually insert a product and sale directly
        // to bypass the domain layers that are already unit-tested elsewhere.
        
        UUID productId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO final_products (id, name, selling_price_cents, category, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                productId, "Dummy Product", 10000, "GENERAL", true);
                
        // 2. Insert a mock sale referencing this product
        UUID saleId = UUID.randomUUID();
        UUID saleItemId = UUID.randomUUID();
        // Since sale_items doesn't strictly check for sale_id existence in a non-existent sales table yet, this works.
        jdbcTemplate.update("INSERT INTO sale_items (id, sale_id, final_product_id, quantity) VALUES (?, ?, ?, ?)",
                saleItemId, saleId, productId, 1);
                
        // 3. Attempt to HARD DELETE via controller (which uses HardDeleteUseCase and triggers SQL DELETE)
        // It should throw DataIntegrityViolationException (which the GlobalExceptionHandler maps to 409 in the HTTP layer, 
        // but here we are calling the controller method directly in Java, so the exception bubbles up).
        
        assertThatThrownBy(() -> controller.hardDeleteProduct(productId, new com.restaurant.application.usecase.HardDeleteFinalProductUseCase(jdbcTemplate)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
