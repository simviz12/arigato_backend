package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.DistributorOffer;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.DistributorOfferRepository;
import com.restaurant.infrastructure.entity.DistributorOfferEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DistributorOfferRepositoryImpl implements DistributorOfferRepository {

    private final SpringDataDistributorOfferRepository springDataRepository;

    @Override
    @Transactional
    public DistributorOffer save(DistributorOffer offer) {
        BigDecimal qty = offer.getOfferedQuantity().getGrams();
        long priceCents = offer.getOfferedPrice().getCents();
        long calculatedPriceCents = 0;
        if (qty.compareTo(BigDecimal.ZERO) > 0) {
            calculatedPriceCents = BigDecimal.valueOf(priceCents)
                    .divide(qty, 0, RoundingMode.HALF_UP)
                    .longValue();
        }

        DistributorOfferEntity entity = DistributorOfferEntity.builder()
                .id(offer.getId())
                .distributorId(offer.getDistributorId())
                .primaryProductId(offer.getPrimaryProductId())
                .offeredQuantityGrams(qty)
                .offeredPriceCents(priceCents)
                .priceCents(calculatedPriceCents)
                .validFrom(offer.getRegisteredAt())
                .build();

        springDataRepository.save(entity);
        return offer;
    }

    @Override
    public List<DistributorOffer> findByPrimaryProductId(UUID productId) {
        return springDataRepository.findByPrimaryProductId(productId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByDistributorIdAndPrimaryProductId(UUID distributorId, UUID productId) {
        springDataRepository.deleteByDistributorIdAndPrimaryProductId(distributorId, productId);
    }

    private DistributorOffer mapToDomain(DistributorOfferEntity entity) {
        return DistributorOffer.builder()
                .id(entity.getId())
                .distributorId(entity.getDistributorId())
                .primaryProductId(entity.getPrimaryProductId())
                .offeredQuantity(Weight.ofGrams(entity.getOfferedQuantityGrams()))
                .offeredPrice(Money.ofCents(entity.getOfferedPriceCents()))
                .registeredAt(entity.getValidFrom())
                .build();
    }
}
