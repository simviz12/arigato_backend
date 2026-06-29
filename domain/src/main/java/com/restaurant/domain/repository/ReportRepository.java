package com.restaurant.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReportRepository {
    BigDecimal getTotalIncome(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalCostOfGoodsSold(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalExpenses(LocalDateTime startDate, LocalDateTime endDate);
}
