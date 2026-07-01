package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreatePrimaryProductUseCase;
import com.restaurant.application.usecase.GetPrimaryProductUseCase;
import com.restaurant.domain.model.PrimaryProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/primary")
@RequiredArgsConstructor
public class PrimaryProductController {

    private final CreatePrimaryProductUseCase createPrimaryProductUseCase;
    private final GetPrimaryProductUseCase getPrimaryProductUseCase;
    private final com.restaurant.application.usecase.RegisterPurchaseUseCase registerPurchaseUseCase;
    private final com.restaurant.application.usecase.GetProductDistributorRankingUseCase getProductDistributorRankingUseCase;
    private final com.restaurant.application.usecase.ListResaleItemsUseCase listResaleItemsUseCase;
    private final com.restaurant.domain.repository.PurchaseRepository purchaseRepository;

    @lombok.Data
    @lombok.Builder
    public static class PrimaryProductResponseDTO {
        private UUID id;
        private String name;
        private com.restaurant.domain.model.UnitOfMeasure unitOfMeasure;
        private com.restaurant.domain.model.vo.Weight currentStock;
        private Double currentStockGrams;
        private Integer currentStockUnits;
        private com.restaurant.domain.model.vo.Weight minimumStockAlert;
        private Double minimumStockAlertGrams;
        private boolean isResaleItem;
        private UUID preferredDistributorId;
        private Double currentAverageCostPerGram;
    }

    private PrimaryProductResponseDTO mapToDTO(PrimaryProduct p) {
        Double avgCost = 0.0;
        try {
            java.util.List<com.restaurant.domain.model.Purchase> history = purchaseRepository.findByPrimaryProductId(p.getId());
            avgCost = new com.restaurant.domain.strategy.WeightedAverageCostingStrategy()
                    .calculateCostPerGram(p, history).doubleValue();
        } catch (Exception e) {
            // ignore
        }
        return PrimaryProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .unitOfMeasure(p.getUnitOfMeasure())
                .currentStock(p.getCurrentStock())
                .currentStockGrams(p.getCurrentStock() != null ? p.getCurrentStock().getGrams().doubleValue() : 0.0)
                .currentStockUnits(p.getCurrentStockUnits())
                .minimumStockAlert(p.getMinimumStockAlert())
                .minimumStockAlertGrams(p.getMinimumStockAlert() != null ? p.getMinimumStockAlert().getGrams().doubleValue() : 0.0)
                .isResaleItem(p.isResaleItem())
                .preferredDistributorId(p.getPreferredDistributorId())
                .currentAverageCostPerGram(avgCost)
                .build();
    }

    @GetMapping("/resale")
    public ResponseEntity<java.util.List<PrimaryProductResponseDTO>> getResaleItems() {
        java.util.List<PrimaryProductResponseDTO> list = listResaleItemsUseCase.execute().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/distributors/ranking")
    public ResponseEntity<java.util.List<com.restaurant.application.dto.response.DistributorRankingDTO>> getDistributorRanking(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductDistributorRankingUseCase.execute(id));
    }

    @PostMapping
    public ResponseEntity<PrimaryProductResponseDTO> create(@RequestBody CreatePrimaryProductUseCase.Command command) {
        PrimaryProduct created = createPrimaryProductUseCase.execute(command);
        return ResponseEntity.ok(mapToDTO(created));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchase(@PathVariable UUID id, @RequestBody com.restaurant.application.dto.command.RegisterPurchaseCommand command) {
        command.setProductId(id);
        registerPurchaseUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrimaryProductResponseDTO> get(@PathVariable UUID id) {
        return getPrimaryProductUseCase.execute(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<java.util.List<PrimaryProductResponseDTO>> getAll() {
        java.util.List<PrimaryProductResponseDTO> list = getPrimaryProductUseCase.getAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
