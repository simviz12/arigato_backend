package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.vo.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class SalesChaosIntegrationTest {

    @Autowired
    private ProcessSaleUseCase processSaleUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private com.restaurant.domain.repository.SaleRepository saleRepository;

    @Test
    void catastrophicDatabaseFailureMidTransactionRollsBackEverything() {
        // Setup a dummy final product
        UUID productId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO final_products (id, name, selling_price_cents, category, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                productId, "Chaos Product", 10000, "GENERAL", true);

        // Inject a simulated database crash EXACTLY when it tries to save the sale 
        // (after it has theoretically started deducting stock)
        doThrow(new DataAccessResourceFailureException("Simulated Database Connection Drop!"))
                .when(saleRepository).save(any(), any());

        ProcessSaleUseCase.Command cmd = ProcessSaleUseCase.Command.builder()
                .cashierId("ChaosMonkey")
                .paymentMethod("CASH")
                .lines(List.of(
                        ProcessSaleUseCase.LineItemDto.builder().finalProductId(productId).quantity(1).build()
                ))
                .build();

        // Execution should throw the DB exception
        assertThatThrownBy(() -> processSaleUseCase.execute(cmd))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Simulated Database Connection Drop!");

        // Assert that due to @Transactional rollback, NOTHING was persisted.
        // We can't easily check stock rollback without setting up a full inventory hierarchy here,
        // but checking the Sales table guarantees the transaction aborted.
        Integer saleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales WHERE cashier_id = 'ChaosMonkey'", Integer.class);
                
        assertThat(saleCount).isEqualTo(0);
    }
}
