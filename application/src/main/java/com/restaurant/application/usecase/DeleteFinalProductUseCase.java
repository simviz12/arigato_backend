package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.repository.FinalProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteFinalProductUseCase {

    private final FinalProductRepository repository;

    public void execute(UUID id) {
        FinalProduct product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Final product not found"));
        
        // Soft delete
        product.deactivate();
        
        // Just update the entity, we do not issue a DELETE SQL command
        repository.update(product);
    }
}
