package com.restaurant.application.usecase;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.SubproductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateSubproductUseCase {
    private final SubproductRepository subproductRepository;

    /**
     * IMPORTANT ARCHITECTURE NOTE REGARDING RECIPE VERSIONING:
     * 
     * In a robust inventory/costing system, changing a recipe's composition (ingredients or yield) 
     * should NOT retroactively alter the computed costs of past sales. 
     * 
     * A past sale was made based on the raw material cost at that exact time. If a recipe is 
     * changed today (e.g., adding more sugar), we do not want reports from last month to suddenly 
     * recalculate and show different profitability metrics.
     * 
     * Future Iteration: Instead of a direct PUT that overwrites the existing row, recipe 
     * updates should logically create a NEW "version" of the Subproduct (or SubproductRecipe), 
     * while the old version remains immutable for historical cost auditing. For the scope of Day 12, 
     * we are performing a direct update, but this constraint is documented for the upcoming Costing Week.
     */
    @Transactional
    public void execute(UUID subproductId, CreateSubproductUseCase.CreateSubproductCommand command) {
        Subproduct existing = subproductRepository.findById(subproductId)
                .orElseThrow(() -> new IllegalArgumentException("Subproduct not found"));

        // Only allow certain updates, preserving the batch stock
        Subproduct updated = Subproduct.builder()
                .id(existing.getId())
                .name(command.getName())
                .totalYield(Weight.ofGrams(command.getTotalYieldGrams()))
                .preparationMode(PreparationMode.valueOf(command.getPreparationMode()))
                .currentBatchStock(existing.getCurrentBatchStock())
                .build();

        List<SubproductIngredient> updatedIngredients = command.getIngredients().stream()
                .map(dto -> new SubproductIngredient(updated.getId(), dto.getPrimaryProductId(), Weight.ofGrams(dto.getQuantityGrams())))
                .collect(Collectors.toList());

        subproductRepository.update(updated, updatedIngredients);
    }
}
