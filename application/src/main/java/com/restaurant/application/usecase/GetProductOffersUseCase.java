package com.restaurant.application.usecase;

import com.restaurant.application.dto.result.DistributorOfferResult;
import com.restaurant.domain.model.DistributorOffer;
import com.restaurant.domain.repository.DistributorOfferRepository;
import com.restaurant.domain.service.PricePerUnitCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductOffersUseCase {

    private final DistributorOfferRepository offerRepository;

    public List<DistributorOfferResult> execute(UUID productId) {
        List<DistributorOffer> offers = offerRepository.findByPrimaryProductId(productId);

        return offers.stream()
                .map(this::mapToResult)
                // Sort by cheapest cost per gram ascending
                .sorted(Comparator.comparing(DistributorOfferResult::getCostPerGramPesos))
                .collect(Collectors.toList());
    }

    private DistributorOfferResult mapToResult(DistributorOffer offer) {
        BigDecimal costPerGram = PricePerUnitCalculator.calculateCostPerGram(
                offer.getOfferedPrice(), 
                offer.getOfferedQuantity()
        );

        return DistributorOfferResult.builder()
                .id(offer.getId())
                .distributorId(offer.getDistributorId())
                .primaryProductId(offer.getPrimaryProductId())
                .offeredQuantityGrams(offer.getOfferedQuantity().getGrams().doubleValue())
                .offeredPricePesos(offer.getOfferedPrice().getPesos().doubleValue())
                .costPerGramPesos(costPerGram)
                .registeredAt(offer.getRegisteredAt())
                .build();
    }
}
