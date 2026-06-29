package com.restaurant.application.usecase;

import com.restaurant.domain.model.BatchPreparationLog;
import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.BatchPreparationLogRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.PrimaryProductStockModifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrepareBatchSubproductUseCase {

    private final SubproductRepository subproductRepository;
    private final PrimaryProductStockModifier stockModifier;
    private final BatchPreparationLogRepository logRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void execute(UUID subproductId, Double quantityToPrepareGrams, String preparedBy) {
        if (quantityToPrepareGrams <= 0) {
            throw new IllegalArgumentException("Quantity to prepare must be greater than 0");
        }

        Subproduct subproduct = subproductRepository.findById(subproductId)
                .orElseThrow(() -> new IllegalArgumentException("Subproduct not found"));

        if (subproduct.getPreparationMode() != PreparationMode.BATCH) {
            throw new IllegalStateException("Cannot prepare a batch for a subproduct marked as ON_THE_FLY");
        }

        List<SubproductIngredient> recipe = subproductRepository.findIngredientsBySubproductId(subproductId);
        if (recipe.isEmpty()) {
            throw new IllegalStateException("Subproduct has no ingredients defined");
        }

        BigDecimal quantityPrepared = BigDecimal.valueOf(quantityToPrepareGrams);
        BigDecimal totalYield = subproduct.getTotalYield().getGrams();
        BigDecimal scaleRatio = quantityPrepared.divide(totalYield, 6, RoundingMode.HALF_UP);

        // 1. Proportional scaling and raw material deduction
        // stockModifier checks for sufficient stock internally and throws if not enough, 
        // triggering a full atomic rollback of the transaction.
        for (SubproductIngredient ingredient : recipe) {
            BigDecimal requiredGrams = ingredient.getQuantity().getGrams().multiply(scaleRatio).setScale(6, RoundingMode.HALF_UP);
            stockModifier.deductPrimaryProductStock(ingredient.getPrimaryProductId(), Weight.ofGrams(requiredGrams));
        }

        // 2. Increment subproduct batch stock
        subproduct.addBatchStock(Weight.ofGrams(quantityPrepared));
        subproductRepository.update(subproduct, recipe);

        // 3. Log the preparation for traceability
        BatchPreparationLog log = BatchPreparationLog.builder()
                .subproductId(subproductId)
                .quantityPrepared(Weight.ofGrams(quantityPrepared))
                .preparedBy(preparedBy)
                .build();
        logRepository.save(log);
    }
}
