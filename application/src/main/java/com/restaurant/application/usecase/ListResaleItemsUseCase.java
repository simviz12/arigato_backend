package com.restaurant.application.usecase;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.repository.PrimaryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListResaleItemsUseCase {
    private final PrimaryProductRepository primaryProductRepository;

    public List<PrimaryProduct> execute() {
        return primaryProductRepository.findAll().stream()
                .filter(p -> p.getUnitOfMeasure() == UnitOfMeasure.UNIT)
                .collect(Collectors.toList());
    }
}
