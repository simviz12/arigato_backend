package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateDistributorUseCase;
import com.restaurant.application.usecase.GetDistributorUseCase;
import com.restaurant.domain.model.Distributor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/distributors")
@RequiredArgsConstructor
public class DistributorController {

    private final CreateDistributorUseCase createDistributorUseCase;
    private final GetDistributorUseCase getDistributorUseCase;

    @PostMapping
    public ResponseEntity<Distributor> create(@RequestBody CreateDistributorUseCase.Command command) {
        return ResponseEntity.ok(createDistributorUseCase.execute(command));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distributor> get(@PathVariable UUID id) {
        return getDistributorUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Distributor>> getAll() {
        return ResponseEntity.ok(getDistributorUseCase.getAll());
    }
}
