package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.FinalProductRepository;
import com.restaurant.infrastructure.entity.FinalProductComponentEntity;
import com.restaurant.infrastructure.entity.FinalProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FinalProductRepositoryAdapter implements FinalProductRepository {

    private final SpringDataFinalProductRepository jpaRepository;

    @Override
    public void save(FinalProduct product, List<FinalProductComponent> components) {
        FinalProductEntity entity = new FinalProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setSellingPriceCents(product.getSellingPrice().getPesos().multiply(new java.math.BigDecimal("100")).longValue());
        entity.setCategory(product.getCategory());
        entity.setActive(product.isActive());
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setUpdatedAt(java.time.LocalDateTime.now());

        List<FinalProductComponentEntity> componentEntities = components.stream().map(c -> {
            FinalProductComponentEntity ce = new FinalProductComponentEntity();
            ce.setFinalProduct(entity);
            if (c.getReference() instanceof PrimaryComponentRef p) {
                ce.setPrimaryProductId(p.getReferenceId());
            } else if (c.getReference() instanceof SubproductComponentRef s) {
                ce.setSubproductId(s.getReferenceId());
            }
            ce.setQuantityGrams(c.getQuantity().getGrams());
            return ce;
        }).collect(Collectors.toList());

        entity.setComponents(componentEntities);
        jpaRepository.save(entity);
    }

    @Override
    public void update(FinalProduct product) {
        jpaRepository.findById(product.getId()).ifPresent(entity -> {
            entity.setActive(product.isActive());
            entity.setUpdatedAt(java.time.LocalDateTime.now());
            // Intentionally not updating other fields here per simple logic
            jpaRepository.save(entity);
        });
    }

    @Override
    public Optional<FinalProduct> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<FinalProductComponent> findComponentsByProductId(UUID productId) {
        return jpaRepository.findById(productId)
                .map(entity -> entity.getComponents().stream().map(ce ->
                        new FinalProductComponent(
                                entity.getId(),
                                ce.getPrimaryProductId() != null ? new PrimaryComponentRef(ce.getPrimaryProductId()) : new SubproductComponentRef(ce.getSubproductId()),
                                Weight.ofGrams(ce.getQuantityGrams())
                        )
                ).collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Override
    public List<FinalProduct> findAll() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private FinalProduct mapToDomain(FinalProductEntity entity) {
        return FinalProduct.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sellingPrice(Money.ofPesos(entity.getSellingPriceCents().doubleValue() / 100.0))
                .category(entity.getCategory())
                .active(entity.getActive())
                .build();
    }
}
