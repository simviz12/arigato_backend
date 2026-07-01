package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateSubproductUseCase;
import com.restaurant.application.usecase.GetSubproductUseCase;
import com.restaurant.application.usecase.PrepareBatchSubproductUseCase;
import com.restaurant.domain.model.Subproduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subproducts")
@RequiredArgsConstructor
public class SubproductController {

    private final CreateSubproductUseCase createSubproductUseCase;
    private final GetSubproductUseCase getSubproductUseCase;
    private final PrepareBatchSubproductUseCase prepareBatchSubproductUseCase;
    private final com.restaurant.application.usecase.CalculateSubproductCostUseCase calculateSubproductCostUseCase;
    private final com.restaurant.domain.repository.SubproductRepository subproductRepository;
    private final com.restaurant.domain.repository.PrimaryProductRepository primaryProductRepository;

    @lombok.Data
    @lombok.Builder
    public static class SubproductResponseDTO {
        private UUID id;
        private String name;
        private com.restaurant.domain.model.vo.Weight totalYield;
        private Double totalYieldGrams;
        private com.restaurant.domain.model.PreparationMode preparationMode;
        private com.restaurant.domain.model.vo.Weight currentBatchStock;
        private Double currentBatchStockGrams;
        private Double costPerGram;
        private List<SubproductIngredientDTO> recipe;
    }

    @lombok.Data
    @lombok.Builder
    public static class SubproductIngredientDTO {
        private UUID primaryProductId;
        private String primaryProductName;
        private Double quantityGrams;
    }

    private SubproductResponseDTO mapToDTO(Subproduct sp) {
        Double cost = 0.0;
        try {
            cost = calculateSubproductCostUseCase.execute(sp.getId()).getCostPerGramPesos().doubleValue();
        } catch (Exception e) {
            // ignore calculation issues
        }

        List<SubproductIngredientDTO> ingredients = subproductRepository.findIngredientsBySubproductId(sp.getId()).stream()
                .map(ing -> {
                    String name = primaryProductRepository.findById(ing.getPrimaryProductId())
                            .map(com.restaurant.domain.model.PrimaryProduct::getName)
                            .orElse("Materia prima desconocida");
                    return SubproductIngredientDTO.builder()
                            .primaryProductId(ing.getPrimaryProductId())
                            .primaryProductName(name)
                            .quantityGrams(ing.getQuantity().getGrams().doubleValue())
                            .build();
                })
                .collect(Collectors.toList());

        return SubproductResponseDTO.builder()
                .id(sp.getId())
                .name(sp.getName())
                .totalYield(sp.getTotalYield())
                .totalYieldGrams(sp.getTotalYield().getGrams().doubleValue())
                .preparationMode(sp.getPreparationMode())
                .currentBatchStock(sp.getCurrentBatchStock())
                .currentBatchStockGrams(sp.getCurrentBatchStock() != null ? sp.getCurrentBatchStock().getGrams().doubleValue() : null)
                .costPerGram(cost)
                .recipe(ingredients)
                .build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSubproductUseCase.CreateSubproductCommand command) {
        try {
            return ResponseEntity.ok(java.util.Map.of("id", createSubproductUseCase.execute(command).toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubproductResponseDTO> get(@PathVariable UUID id) {
        return getSubproductUseCase.execute(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SubproductResponseDTO>> getAll() {
        List<SubproductResponseDTO> list = getSubproductUseCase.getAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    public static class PrepareBatchCommand {
        public Double quantity;
    }

    @PostMapping("/{id}/prepare")
    public ResponseEntity<?> prepareBatch(@PathVariable UUID id, @RequestBody PrepareBatchCommand command) {
        try {
            prepareBatchSubproductUseCase.execute(id, command.quantity, "System"); // Or user from context
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
