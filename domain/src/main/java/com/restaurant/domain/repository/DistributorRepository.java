package com.restaurant.domain.repository;

import com.restaurant.domain.model.Distributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DistributorRepository {
    Distributor save(Distributor distributor);
    Optional<Distributor> findById(UUID id);
    List<Distributor> findAll();
    void delete(Distributor distributor);
}
