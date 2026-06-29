package com.restaurant.api.controller;

import com.restaurant.application.dto.command.RegisterPurchaseCommand;
import com.restaurant.application.usecase.RegisterPurchaseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.restaurant.application.audit.Auditable;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final RegisterPurchaseUseCase registerPurchaseUseCase;

    @PostMapping
    @Auditable(action = "PURCHASE_REGISTERED")
    public ResponseEntity<Void> registerPurchase(@RequestBody RegisterPurchaseCommand command) {
        registerPurchaseUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
