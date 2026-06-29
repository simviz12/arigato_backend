package com.restaurant.api.controller;

import com.restaurant.application.usecase.ProcessSaleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final ProcessSaleUseCase processSaleUseCase;

    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody ProcessSaleUseCase.Command command) {
        try {
            // Default cashier if not provided
            if (command.getCashierId() == null) {
                command.setCashierId("System"); 
            }
            UUID saleId = processSaleUseCase.execute(command);
            return ResponseEntity.ok(java.util.Map.of("saleId", saleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
