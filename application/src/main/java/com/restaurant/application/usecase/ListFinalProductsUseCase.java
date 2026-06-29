package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.repository.FinalProductRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListFinalProductsUseCase {
    private final FinalProductRepository finalProductRepository;
    private final GetFinalProductMarginUseCase getFinalProductMarginUseCase;

    @Data
    @Builder
    public static class FinalProductDto {
        private UUID id;
        private String name;
        private BigDecimal sellingPricePesos;
        private BigDecimal marginPercentage;
        private String category;
        private boolean active;
    }

    public List<FinalProductDto> execute() {
        return finalProductRepository.findAll().stream()
                .filter(p -> p.isActive())
                .map(p -> {
                    BigDecimal margin = BigDecimal.ZERO;
                    try {
                        margin = getFinalProductMarginUseCase.execute(p.getId()).getMarginPercentage();
                    } catch (Exception e) {
                        // Ignore margin calculation errors for list view
                    }
                    return FinalProductDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .sellingPricePesos(p.getSellingPrice().getPesos())
                            .category(p.getCategory())
                            .marginPercentage(margin)
                            .active(p.isActive())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
