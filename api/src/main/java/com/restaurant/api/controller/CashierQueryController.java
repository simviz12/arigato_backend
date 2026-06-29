package com.restaurant.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
public class CashierQueryController {

    // Mocking the CQRS Read Model for demonstration
    // In Week 5 we built the daily snapshot for all products, this is similar but grouped by payment method
    @GetMapping("/session-summary")
    public ResponseEntity<?> getSessionSummary() {
        return ResponseEntity.ok(Map.of(
                "totalSalesCount", 45,
                "totalRevenue", 1250000,
                "breakdown", Map.of(
                        "EFECTIVO", 800000,
                        "NEQUI", 400000,
                        "MIXTO", 50000 // In reality, mixto is split into its cash/nequi parts, or tracked separately
                )
        ));
    }
}
