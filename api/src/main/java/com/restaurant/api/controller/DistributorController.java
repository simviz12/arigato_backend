package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateDistributorUseCase;
import com.restaurant.application.usecase.GetDistributorUseCase;
import com.restaurant.domain.model.Distributor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/distributors")
@RequiredArgsConstructor
public class DistributorController {

    private final CreateDistributorUseCase createDistributorUseCase;
    private final GetDistributorUseCase getDistributorUseCase;
    private final com.restaurant.application.usecase.SaveDistributorOfferUseCase saveDistributorOfferUseCase;

    public static class SaveOfferRequest {
        public UUID primaryProductId;
        public Double offeredQuantityGrams;
        public Double offeredPricePesos;
    }

    @PostMapping("/{id}/offers")
    public ResponseEntity<?> saveOffer(@PathVariable UUID id, @RequestBody SaveOfferRequest request) {
        try {
            saveDistributorOfferUseCase.execute(
                    id,
                    request.primaryProductId,
                    request.offeredQuantityGrams,
                    request.offeredPricePesos
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Distributor> create(@RequestBody CreateDistributorUseCase.Command command) {
        return ResponseEntity.ok(createDistributorUseCase.execute(command));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distributor> get(@PathVariable UUID id) {
        return getDistributorUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Distributor>> getAll() {
        return ResponseEntity.ok(getDistributorUseCase.getAll());
    }
}
