package com.restaurant.application.query;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReconciliationIntegrationTest {

    @Autowired
    private ReconciliationQueryService reconciliationService;

    @Test
    void financialAuditPassesWithZeroDiscrepancy() {
        // Run the script against the seeded database
        ReconciliationQueryService.ReconciliationReport report = reconciliationService.runFinancialAudit();
        
        // Assert mathematical purity
        assertThat(report.isBalanced()).isTrue();
        
        // Profit must exactly equal Revenue - COGS
        BigDecimal expectedProfit = report.totalRevenue().subtract(report.totalCogs());
        assertThat(report.grossProfit()).isEqualByComparingTo(expectedProfit);
    }
}
