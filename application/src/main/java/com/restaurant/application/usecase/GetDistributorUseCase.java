package com.restaurant.application.usecase;

import com.restaurant.domain.model.Distributor;
import com.restaurant.domain.repository.DistributorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDistributorUseCase {
    private final DistributorRepository distributorRepository;

    public Optional<Distributor> execute(UUID id) {
        return distributorRepository.findById(id);
    }
    
    public List<Distributor> getAll() {
        return distributorRepository.findAll();
    }
}
