package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.repository.FinalProductRepository;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.FinalProductCostCalculator;
import com.restaurant.domain.strategy.CostingStrategy;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFinalProductMarginUseCase {

    private final FinalProductRepository finalProductRepository;
    private final PrimaryProductRepository primaryProductRepository;
    private final PurchaseRepository purchaseRepository;
    private final SubproductRepository subproductRepository;

    @Data
    @Builder
    public static class MarginResponse {
        private BigDecimal costPerUnit;
        private BigDecimal sellingPrice;
        private BigDecimal marginAmount;
        private BigDecimal marginPercentage;
    }

    public MarginResponse execute(UUID finalProductId) {
        FinalProduct product = finalProductRepository.findById(finalProductId)
                .orElseThrow(() -> new IllegalArgumentException("Final product not found"));
        
        List<FinalProductComponent> components = finalProductRepository.findComponentsByProductId(finalProductId);

        // Fetch all primary products and their purchase history to compute cost
        List<PrimaryProduct> primaryProducts = primaryProductRepository.findAll();
        Map<UUID, PrimaryProduct> primaryProductsMap = new HashMap<>();
        Map<UUID, List<Purchase>> purchasesMap = new HashMap<>();
        
        for (PrimaryProduct pp : primaryProducts) {
            primaryProductsMap.put(pp.getId(), pp);
            purchasesMap.put(pp.getId(), purchaseRepository.findByPrimaryProductId(pp.getId()));
        }

        // Fetch subproducts and their recipes
        // For a real production app, we would only fetch the ones we need, but for simplicity we fetch all here
        List<Subproduct> subproducts = subproductRepository.findAll();
        Map<UUID, Subproduct> subproductsMap = new HashMap<>();
        Map<UUID, List<SubproductIngredient>> subproductRecipesMap = new HashMap<>();
        
        for (Subproduct sp : subproducts) {
            subproductsMap.put(sp.getId(), sp);
            subproductRecipesMap.put(sp.getId(), subproductRepository.findIngredientsBySubproductId(sp.getId()));
        }

        CostingStrategy costingStrategy = new WeightedAverageCostingStrategy();
        
        Money totalCost = FinalProductCostCalculator.calculate(
                components, primaryProductsMap, purchasesMap, subproductsMap, subproductRecipesMap, costingStrategy
        );

        BigDecimal cost = totalCost.getPesos();
        BigDecimal selling = product.getSellingPrice().getPesos();
        BigDecimal marginAmount = selling.subtract(cost);
        
        BigDecimal marginPercentage = BigDecimal.ZERO;
        if (selling.compareTo(BigDecimal.ZERO) > 0) {
            marginPercentage = marginAmount.divide(selling, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }

        return MarginResponse.builder()
                .costPerUnit(cost)
                .sellingPrice(selling)
                .marginAmount(marginAmount)
                .marginPercentage(marginPercentage.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
