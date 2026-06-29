package com.restaurant.application.controller;

import com.restaurant.application.usecase.FinancialReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final FinancialReportUseCase financialReportUseCase;

    @GetMapping
    public ResponseEntity<FinancialReportUseCase.ReportResponse> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        FinancialReportUseCase.ReportResponse report = financialReportUseCase.execute(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
