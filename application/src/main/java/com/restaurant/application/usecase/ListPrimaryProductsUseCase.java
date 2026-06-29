package com.restaurant.application.usecase;

import com.restaurant.application.dto.result.PrimaryProductResult;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.repository.PrimaryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListPrimaryProductsUseCase {
    private final PrimaryProductRepository repository;

    public List<PrimaryProductResult> execute() {
        return repository.findAll().stream()
                .map(this::mapToResult)
                .collect(Collectors.toList());
    }

    private PrimaryProductResult mapToResult(PrimaryProduct product) {
        return PrimaryProductResult.builder()
                .id(product.getId())
                .name(product.getName())
                .unitOfMeasure(product.getUnitOfMeasure().name())
                .currentStockGrams(product.getCurrentStock().getGrams().doubleValue())
                .currentStockUnits(product.getCurrentStockUnits())
                .minimumStockAlert(product.getMinimumStockAlert() != null ? product.getMinimumStockAlert().getGrams().doubleValue() : null)
                .isResaleItem(product.isResaleItem())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
