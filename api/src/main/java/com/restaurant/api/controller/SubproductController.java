package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateSubproductUseCase;
import com.restaurant.application.usecase.GetSubproductUseCase;
import com.restaurant.application.usecase.PrepareBatchSubproductUseCase;
import com.restaurant.domain.model.Subproduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subproducts")
@RequiredArgsConstructor
public class SubproductController {

    // Note: CreateSubproductUseCase might already exist with a specific signature
    // Assuming a simple CreateSubproductUseCase for this example or adapting it.
    private final CreateSubproductUseCase createSubproductUseCase;
    private final GetSubproductUseCase getSubproductUseCase;
    private final PrepareBatchSubproductUseCase prepareBatchSubproductUseCase;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSubproductUseCase.CreateSubproductCommand command) {
        try {
            return ResponseEntity.ok(java.util.Map.of("id", createSubproductUseCase.execute(command).toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subproduct> get(@PathVariable UUID id) {
        return getSubproductUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Subproduct>> getAll() {
        return ResponseEntity.ok(getSubproductUseCase.getAll());
    }

    public static class PrepareBatchCommand {
        public Double quantity;
    }

    @PostMapping("/{id}/prepare")
    public ResponseEntity<?> prepareBatch(@PathVariable UUID id, @RequestBody PrepareBatchCommand command) {
        try {
            prepareBatchSubproductUseCase.execute(id, command.quantity, "System"); // Or user from context
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
