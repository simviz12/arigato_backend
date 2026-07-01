package com.restaurant.infrastructure.repository;

import org.springframework.stereotype.Component;

import com.restaurant.domain.repository.*;
import com.restaurant.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DummyAdapters {

    // DummyFinalProductRepository removed
    // DummySaleRepository removed

    // @Component
    public static class DummySubproductRepository implements SubproductRepository {
        public Subproduct save(Subproduct subproduct, List<SubproductIngredient> ingredients) { return subproduct; }
        public Subproduct update(Subproduct subproduct, List<SubproductIngredient> ingredients) { return subproduct; }
        public Optional<Subproduct> findById(UUID id) { return Optional.empty(); }
        public List<SubproductIngredient> findIngredientsBySubproductId(UUID subproductId) { return List.of(); }
        public List<Subproduct> findAll() { return List.of(); }
    }
    
    // @Component
    public static class DummyDistributorOfferRepository implements DistributorOfferRepository {
        public DistributorOffer save(DistributorOffer offer) { return offer; }
        public List<DistributorOffer> findByPrimaryProductId(UUID productId) { return List.of(); }
        public void deleteByDistributorIdAndPrimaryProductId(UUID distributorId, UUID productId) {}
    }
    
    @Component
    public static class DummyBatchPreparationLogRepository implements BatchPreparationLogRepository {
        public BatchPreparationLog save(BatchPreparationLog log) { return log; }
        public List<BatchPreparationLog> findBySubproductId(UUID subproductId) { return List.of(); }
    }
}
