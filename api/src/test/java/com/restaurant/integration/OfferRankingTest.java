package com.restaurant.integration;

import com.restaurant.application.dto.result.DistributorOfferResult;
import com.restaurant.application.usecase.GetProductOffersUseCase;
import com.restaurant.domain.model.DistributorOffer;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.DistributorOfferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OfferRankingTest {

    @Autowired
    private GetProductOffersUseCase getProductOffersUseCase;

    @Autowired
    private DistributorOfferRepository offerRepository;

    @Test
    void shouldReturnOffersSortedByCostPerGramAscending() {
        UUID productId = UUID.randomUUID();

        // Offer 1: 100 pesos / 10 grams = 10 pesos/gram (Most Expensive)
        offerRepository.save(DistributorOffer.builder()
                .distributorId(UUID.randomUUID())
                .primaryProductId(productId)
                .offeredQuantity(Weight.ofGrams(10))
                .offeredPrice(Money.ofPesos(100))
                .build());

        // Offer 2: 50 pesos / 20 grams = 2.5 pesos/gram (Cheapest)
        offerRepository.save(DistributorOffer.builder()
                .distributorId(UUID.randomUUID())
                .primaryProductId(productId)
                .offeredQuantity(Weight.ofGrams(20))
                .offeredPrice(Money.ofPesos(50))
                .build());

        // Offer 3: 15 pesos / 3 grams = 5 pesos/gram (Middle)
        offerRepository.save(DistributorOffer.builder()
                .distributorId(UUID.randomUUID())
                .primaryProductId(productId)
                .offeredQuantity(Weight.ofGrams(3))
                .offeredPrice(Money.ofPesos(15))
                .build());

        List<DistributorOfferResult> results = getProductOffersUseCase.execute(productId);

        assertThat(results).hasSize(3);
        
        // Assert strict ordering by costPerGramPesos
        assertThat(results.get(0).getCostPerGramPesos()).isEqualByComparingTo("2.5");
        assertThat(results.get(1).getCostPerGramPesos()).isEqualByComparingTo("5.0");
        assertThat(results.get(2).getCostPerGramPesos()).isEqualByComparingTo("10.0");
    }
}
