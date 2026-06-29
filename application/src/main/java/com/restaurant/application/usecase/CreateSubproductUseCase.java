package com.restaurant.application.usecase;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.SubproductRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateSubproductUseCase {
    private final SubproductRepository subproductRepository;
    private final com.restaurant.domain.repository.PrimaryProductRepository primaryProductRepository;

    public UUID execute(CreateSubproductCommand command) {
        
        // 1. Validate ingredients are not UNIT based
        for (IngredientDto dto : command.getIngredients()) {
            com.restaurant.domain.model.PrimaryProduct pp = primaryProductRepository.findById(dto.getPrimaryProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
            if (pp.getUnitOfMeasure() == com.restaurant.domain.model.UnitOfMeasure.UNIT) {
                throw new IllegalArgumentException("Cannot use a UNIT-based resale item (like bottled drinks) as an ingredient in a recipe.");
            }
        }

        Subproduct subproduct = Subproduct.builder()
                .name(command.getName())
                .totalYield(Weight.ofGrams(command.getTotalYieldGrams()))
                .preparationMode(PreparationMode.valueOf(command.getPreparationMode()))
                .build();

        List<SubproductIngredient> ingredients = command.getIngredients().stream()
                .map(dto -> new SubproductIngredient(subproduct.getId(), dto.getPrimaryProductId(), Weight.ofGrams(dto.getQuantityGrams())))
                .collect(Collectors.toList());

        Subproduct saved = subproductRepository.save(subproduct, ingredients);
        return saved.getId();
    }

    @Data
    public static class CreateSubproductCommand {
        private String name;
        private Double totalYieldGrams;
        private String preparationMode;
        private List<IngredientDto> ingredients;
    }

    @Data
    public static class IngredientDto {
        private UUID primaryProductId;
        private Double quantityGrams;
    }
}
