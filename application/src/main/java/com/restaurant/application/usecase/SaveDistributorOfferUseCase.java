package com.restaurant.application.usecase;

import com.restaurant.domain.model.DistributorOffer;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.DistributorOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveDistributorOfferUseCase {

    private final DistributorOfferRepository offerRepository;

    @Transactional
    public void execute(UUID distributorId, UUID productId, Double quantityGrams, Double pricePesos) {
        if (quantityGrams == null || quantityGrams <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (pricePesos == null || pricePesos <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // Delete any existing offer for this distributor/product combo
        offerRepository.deleteByDistributorIdAndPrimaryProductId(distributorId, productId);

        DistributorOffer offer = DistributorOffer.builder()
                .id(UUID.randomUUID())
                .distributorId(distributorId)
                .primaryProductId(productId)
                .offeredQuantity(Weight.ofGrams(BigDecimal.valueOf(quantityGrams)))
                .offeredPrice(Money.ofPesos(pricePesos))
                .registeredAt(LocalDateTime.now())
                .build();

        offerRepository.save(offer);
    }
}
