package com.restaurant.api.controller;

import com.restaurant.application.query.AnalyticsQueryService;
import com.restaurant.application.query.DistributorRankingService;
import com.restaurant.application.port.out.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;
    private final DistributorRankingService distributorRankingService;
    private final PdfReportGenerator pdfReportGenerator;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getSummary(from, to));
    }

    @GetMapping("/timeseries")
    public ResponseEntity<?> getTimeSeries(
            @RequestParam(defaultValue = "day") String granularity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getTimeSeries(granularity, from, to));
    }

    @GetMapping("/comparison")
    public ResponseEntity<?> getComparison(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime referenceDate,
            @RequestParam(defaultValue = "partial") String comparisonMode
    ) {
        if (referenceDate == null) {
            referenceDate = LocalDateTime.now();
        }
        return ResponseEntity.ok(analyticsQueryService.getComparison(period, referenceDate, comparisonMode));
    }

    @GetMapping("/price-trends/{primaryProductId}")
    public ResponseEntity<?> getPriceTrends(@PathVariable String primaryProductId) {
        return ResponseEntity.ok(analyticsQueryService.getPriceTrends(primaryProductId));
    }

    @GetMapping("/best-distributors")
    public ResponseEntity<?> getBestDistributors(@RequestParam(defaultValue = "false") boolean lowStock) {
        return ResponseEntity.ok(distributorRankingService.getBestDistributorPerProduct(lowStock, null));
    }

    @GetMapping("/best-distributors/{productId}")
    public ResponseEntity<?> getRankedDistributorsForProduct(@PathVariable String productId) {
        return ResponseEntity.ok(distributorRankingService.getRankedDistributorsForProduct(productId));
    }

    @GetMapping("/best-distributors/all")
    public ResponseEntity<?> getAllRankings() {
        return ResponseEntity.ok(distributorRankingService.getAllRankings());
    }

    @GetMapping("/best-distributors/export-pdf")
    public ResponseEntity<byte[]> exportBestDistributorsPdf(@RequestParam(defaultValue = "false") boolean lowStock) {
        List<Map<String, Object>> rankingData = distributorRankingService.getBestDistributorPerProduct(lowStock, null);
        Map<String, Object> savingsData = distributorRankingService.calculatePotentialSavingsLast30Days();
        
        byte[] pdfBytes = pdfReportGenerator.generateBestDistributorsReport(rankingData, savingsData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"proveedores-recomendados.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/best-distributors/export-pdf-custom")
    public ResponseEntity<byte[]> exportCustomPdf(@RequestBody List<String> productIds) {
        List<Map<String, Object>> rankingData = distributorRankingService.getBestDistributorPerProduct(false, productIds);
        Map<String, Object> savingsData = distributorRankingService.calculatePotentialSavingsLast30Days();
        
        byte[] pdfBytes = pdfReportGenerator.generateBestDistributorsReport(rankingData, savingsData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"lista-compras-personalizada.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
