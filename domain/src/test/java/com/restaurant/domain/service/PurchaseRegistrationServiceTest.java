package com.restaurant.domain.service;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseRegistrationServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PrimaryProductRepository primaryProductRepository;

    @InjectMocks
    private PurchaseRegistrationService service;

    private UUID productId;
    private UUID distributorId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        distributorId = UUID.randomUUID();
    }

    @Test
    void shouldRejectNullQuantity() {
        assertThatThrownBy(() -> service.registerPurchase(productId, distributorId, null, Money.ofCents(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> service.registerPurchase(productId, distributorId, Weight.ofGrams(0), Money.ofCents(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> service.registerPurchase(productId, distributorId, Weight.ofGrams(100), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThatThrownBy(() -> service.registerPurchase(productId, distributorId, Weight.ofGrams(100), Money.ofCents(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    void shouldRejectIfProductNotFound() {
        when(primaryProductRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerPurchase(productId, distributorId, Weight.ofGrams(100), Money.ofCents(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldRegisterPurchaseAndIncrementStock() {
        PrimaryProduct product = PrimaryProduct.builder()
                .id(productId)
                .name("Tomato")
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .currentStock(Weight.ofGrams(500))
                .build();

        when(primaryProductRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Weight addedQuantity = Weight.ofGrams(1000);
        Money price = Money.ofPesos(5000);

        Purchase result = service.registerPurchase(productId, distributorId, addedQuantity, price);

        // Verify purchase object
        assertThat(result.getPrimaryProductId()).isEqualTo(productId);
        assertThat(result.getQuantityGrams()).isEqualTo(addedQuantity);
        assertThat(result.getTotalPrice()).isEqualTo(price);

        // Verify stock incremented
        assertThat(product.getCurrentStock().getGrams()).isEqualByComparingTo(new BigDecimal("1500"));

        // Verify repository calls
        verify(purchaseRepository).save(any(Purchase.class));
        verify(primaryProductRepository).save(product);
    }
}
