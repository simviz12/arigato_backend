package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.infrastructure.entity.PrimaryProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PrimaryProductRepositoryAdapter implements PrimaryProductRepository {

    private final SpringDataPrimaryProductRepository springDataPrimaryProductRepository;

    @Override
    public PrimaryProduct save(PrimaryProduct product) {
        PrimaryProductEntity entity = PrimaryProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .unitOfMeasure(product.getUnitOfMeasure())
                .currentStockGrams(product.getCurrentStock().getGrams())
                .currentStockUnits(product.getCurrentStockUnits())
                .minimumStockAlert(product.getMinimumStockAlert() != null ? product.getMinimumStockAlert().getGrams() : null)
                .isResaleItem(product.isResaleItem())
                .build();
        
        PrimaryProductEntity saved = springDataPrimaryProductRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<PrimaryProduct> findById(UUID id) {
        return springDataPrimaryProductRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public java.util.List<PrimaryProduct> findAll() {
        return springDataPrimaryProductRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<PrimaryProduct> findByName(String name) {
        return springDataPrimaryProductRepository.findByName(name).map(this::mapToDomain);
    }

    private PrimaryProduct mapToDomain(PrimaryProductEntity entity) {
        return PrimaryProduct.builder()
                .id(entity.getId())
                .name(entity.getName())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .currentStock(com.restaurant.domain.model.vo.Weight.ofGrams(entity.getCurrentStockGrams()))
                .currentStockUnits(entity.getCurrentStockUnits())
                .minimumStockAlert(entity.getMinimumStockAlert() != null ? com.restaurant.domain.model.vo.Weight.ofGrams(entity.getMinimumStockAlert()) : null)
                .isResaleItem(entity.isResaleItem())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
