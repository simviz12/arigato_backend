package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.PurchaseRepository;
import com.restaurant.infrastructure.entity.PurchaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final SpringDataPurchaseRepository jpaPurchaseRepository;

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchase.getId());
        entity.setPrimaryProductId(purchase.getPrimaryProductId());
        entity.setDistributorId(purchase.getDistributorId());
        entity.setPurchaseDate(purchase.getPurchaseDate());
        entity.setQuantity(purchase.getQuantity().getGrams());
        entity.setTotalPriceCents(purchase.getTotalPrice().getCents());

        PurchaseEntity saved = jpaPurchaseRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Purchase> findById(UUID id) {
        return jpaPurchaseRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public java.util.List<Purchase> findByPrimaryProductId(UUID primaryProductId) {
        return jpaPurchaseRepository.findByPrimaryProductId(primaryProductId).stream()
                .map(this::mapToDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    private Purchase mapToDomain(PurchaseEntity entity) {
        return Purchase.builder()
                .id(entity.getId())
                .primaryProductId(entity.getPrimaryProductId())
                .distributorId(entity.getDistributorId())
                .purchaseDate(entity.getPurchaseDate())
                .quantity(Weight.ofGrams(entity.getQuantity()))
                .totalPrice(Money.ofCents(entity.getTotalPriceCents()))
                .build();
    }
}
