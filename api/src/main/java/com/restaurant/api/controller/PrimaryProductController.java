package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreatePrimaryProductUseCase;
import com.restaurant.application.usecase.GetPrimaryProductUseCase;
import com.restaurant.domain.model.PrimaryProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/primary")
@RequiredArgsConstructor
public class PrimaryProductController {

    private final CreatePrimaryProductUseCase createPrimaryProductUseCase;
    private final GetPrimaryProductUseCase getPrimaryProductUseCase;
    private final com.restaurant.application.usecase.RegisterPurchaseUseCase registerPurchaseUseCase;
    private final com.restaurant.application.usecase.GetProductDistributorRankingUseCase getProductDistributorRankingUseCase;
    private final com.restaurant.application.usecase.ListResaleItemsUseCase listResaleItemsUseCase;

    @GetMapping("/resale")
    public ResponseEntity<java.util.List<PrimaryProduct>> getResaleItems() {
        return ResponseEntity.ok(listResaleItemsUseCase.execute());
    }

    @GetMapping("/{id}/distributors/ranking")
    public ResponseEntity<java.util.List<com.restaurant.application.dto.response.DistributorRankingDTO>> getDistributorRanking(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductDistributorRankingUseCase.execute(id));
    }

    @PostMapping
    public ResponseEntity<PrimaryProduct> create(@RequestBody CreatePrimaryProductUseCase.Command command) {
        return ResponseEntity.ok(createPrimaryProductUseCase.execute(command));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchase(@PathVariable UUID id, @RequestBody com.restaurant.application.dto.command.RegisterPurchaseCommand command) {
        command.setProductId(id);
        registerPurchaseUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrimaryProduct> get(@PathVariable UUID id) {
        return getPrimaryProductUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<java.util.List<PrimaryProduct>> getAll() {
        return ResponseEntity.ok(getPrimaryProductUseCase.getAll());
    }
}
