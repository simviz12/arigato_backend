package com.restaurant.application.usecase;

import com.restaurant.application.dto.response.DistributorRankingDTO;
import com.restaurant.domain.model.Distributor;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.repository.DistributorRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductDistributorRankingUseCase {

    private final PurchaseRepository purchaseRepository;
    private final DistributorRepository distributorRepository;

    public List<DistributorRankingDTO> execute(UUID primaryProductId) {
        List<Purchase> purchases = purchaseRepository.findByPrimaryProductId(primaryProductId);

        Map<UUID, List<Purchase>> purchasesByDistributor = purchases.stream()
                .collect(Collectors.groupingBy(Purchase::getDistributorId));

        return purchasesByDistributor.entrySet().stream()
                .map(entry -> {
                    UUID distributorId = entry.getKey();
                    List<Purchase> distPurchases = entry.getValue();

                    BigDecimal totalPesos = BigDecimal.ZERO;
                    BigDecimal totalGrams = BigDecimal.ZERO;

                    for (Purchase p : distPurchases) {
                        totalPesos = totalPesos.add(p.getTotalPrice().getPesos());
                        totalGrams = totalGrams.add(p.getQuantity().getGrams());
                    }

                    BigDecimal averagePricePerGram = BigDecimal.ZERO;
                    if (totalGrams.compareTo(BigDecimal.ZERO) > 0) {
                        averagePricePerGram = totalPesos.divide(totalGrams, 4, RoundingMode.HALF_UP);
                    }

                    String distributorName = distributorRepository.findById(distributorId)
                            .map(Distributor::getName)
                            .orElse("Distribuidor Desconocido");

                    return DistributorRankingDTO.builder()
                            .distributorId(distributorId)
                            .distributorName(distributorName)
                            .averagePricePerGram(averagePricePerGram)
                            .totalPurchases(distPurchases.size())
                            .build();
                })
                .sorted(Comparator.comparing(DistributorRankingDTO::getAveragePricePerGram))
                .collect(Collectors.toList());
    }
}
