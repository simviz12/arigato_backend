package com.restaurant.application.usecase;

import com.restaurant.domain.repository.ReportRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinancialReportUseCase {

    private final ReportRepository reportRepository;

    @Data
    @Builder
    public static class ReportResponse {
        private BigDecimal income;
        private BigDecimal costOfGoodsSold;
        private BigDecimal expenses;
        private BigDecimal grossProfit;
        private BigDecimal netProfit;
    }

    public ReportResponse execute(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal income = reportRepository.getTotalIncome(startDate, endDate);
        BigDecimal cogs = reportRepository.getTotalCostOfGoodsSold(startDate, endDate);
        BigDecimal expenses = reportRepository.getTotalExpenses(startDate, endDate);
        
        if (income == null) income = BigDecimal.ZERO;
        if (cogs == null) cogs = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;

        BigDecimal grossProfit = income.subtract(cogs);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        return ReportResponse.builder()
                .income(income)
                .costOfGoodsSold(cogs)
                .expenses(expenses)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .build();
    }
}
