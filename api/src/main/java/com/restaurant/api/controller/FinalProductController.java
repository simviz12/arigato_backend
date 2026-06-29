package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateFinalProductUseCase;
import com.restaurant.application.usecase.ListFinalProductsUseCase;
import com.restaurant.domain.model.FinalProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/final")
@RequiredArgsConstructor
public class FinalProductController {

    private final CreateFinalProductUseCase createFinalProductUseCase;
    private final ListFinalProductsUseCase listFinalProductsUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateFinalProductUseCase.Command command) {
        try {
            UUID id = createFinalProductUseCase.execute(command);
            return ResponseEntity.ok(java.util.Map.of("id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ListFinalProductsUseCase.FinalProductDto>> getAll() {
        return ResponseEntity.ok(listFinalProductsUseCase.execute());
    }
}
