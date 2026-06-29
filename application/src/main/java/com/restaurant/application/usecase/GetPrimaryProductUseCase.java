package com.restaurant.application.usecase;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.repository.PrimaryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetPrimaryProductUseCase {
    private final PrimaryProductRepository primaryProductRepository;

    public Optional<PrimaryProduct> execute(UUID id) {
        return primaryProductRepository.findById(id);
    }

    public java.util.List<PrimaryProduct> getAll() {
        return primaryProductRepository.findAll();
    }
}
