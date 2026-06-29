package com.restaurant.application.usecase;

import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.repository.SubproductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSubproductUseCase {
    private final SubproductRepository subproductRepository;

    public Optional<Subproduct> execute(UUID id) {
        return subproductRepository.findById(id);
    }
    
    public List<Subproduct> getAll() {
        return subproductRepository.findAll();
    }
}
