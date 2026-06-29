package com.restaurant.domain.service;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class PurchaseRegistrationService {

    private final PurchaseRepository purchaseRepository;
    private final PrimaryProductRepository primaryProductRepository;

    public Purchase registerPurchase(UUID productId, UUID distributorId, Weight quantity, Money price) {
        if (quantity == null || quantity.getGrams().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly positive");
        }
        if (price == null || price.getCents() <= 0) {
            throw new IllegalArgumentException("Price must be strictly positive");
        }

        PrimaryProduct product = primaryProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Purchase purchase = Purchase.builder()
                .primaryProductId(productId)
                .distributorId(distributorId)
                .quantity(quantity)
                .totalPrice(price)
                .build();

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Atomically increment the stock
        product.addStock(quantity);
        primaryProductRepository.save(product);

        return savedPurchase;
    }
}
