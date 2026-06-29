package com.restaurant.application.usecase;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductCostResult;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.SubproductCostCalculator;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalculateSubproductCostUseCase {

    private final SubproductRepository subproductRepository;
    private final PrimaryProductRepository primaryProductRepository;
    private final PurchaseRepository purchaseRepository;

    public SubproductCostResult execute(UUID subproductId) {
        Subproduct subproduct = subproductRepository.findById(subproductId)
                .orElseThrow(() -> new IllegalArgumentException("Subproduct not found"));

        List<SubproductIngredient> recipe = subproductRepository.findIngredientsBySubproductId(subproductId);

        Map<UUID, PrimaryProduct> productsMap = new HashMap<>();
        Map<UUID, List<Purchase>> purchasesMap = new HashMap<>();

        for (SubproductIngredient ingredient : recipe) {
            UUID productId = ingredient.getPrimaryProductId();
            primaryProductRepository.findById(productId).ifPresent(p -> {
                productsMap.put(productId, p);
                purchasesMap.put(productId, purchaseRepository.findByPrimaryProductId(productId));
            });
        }

        // We use Weighted Average as default, but this can easily be injected or swapped.
        WeightedAverageCostingStrategy strategy = new WeightedAverageCostingStrategy();

        return SubproductCostCalculator.calculate(subproduct, recipe, productsMap, purchasesMap, strategy);
    }
}
