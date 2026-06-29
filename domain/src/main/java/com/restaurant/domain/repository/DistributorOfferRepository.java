package com.restaurant.domain.repository;

import com.restaurant.domain.model.DistributorOffer;

import java.util.List;
import java.util.UUID;

public interface DistributorOfferRepository {
    DistributorOffer save(DistributorOffer offer);
    List<DistributorOffer> findByPrimaryProductId(UUID productId);
    void deleteByDistributorIdAndPrimaryProductId(UUID distributorId, UUID productId);
}
