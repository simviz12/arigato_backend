package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.Distributor;
import com.restaurant.domain.repository.DistributorRepository;
import com.restaurant.infrastructure.entity.DistributorEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DistributorRepositoryAdapter implements DistributorRepository {

    private final SpringDataDistributorRepository springDataDistributorRepository;

    @Override
    public Distributor save(Distributor distributor) {
        DistributorEntity entity = DistributorEntity.builder()
                .id(distributor.getId())
                .name(distributor.getName())
                .contactPhone(distributor.getContactPhone())
                .contactEmail(distributor.getContactEmail())
                .notes(distributor.getNotes())
                .active(distributor.isActive())
                .build();
        
        DistributorEntity saved = springDataDistributorRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Distributor> findById(UUID id) {
        return springDataDistributorRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public java.util.List<Distributor> findAll() {
        return springDataDistributorRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void delete(Distributor distributor) {
        springDataDistributorRepository.deleteById(distributor.getId());
    }

    private Distributor mapToDomain(DistributorEntity entity) {
        return Distributor.builder()
                .id(entity.getId())
                .name(entity.getName())
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .notes(entity.getNotes())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
