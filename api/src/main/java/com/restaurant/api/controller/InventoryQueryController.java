package com.restaurant.api.controller;

import com.restaurant.application.query.GetDailyInventorySnapshotQuery;
import com.restaurant.application.query.GetProductMovementsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryQueryController {

    private final GetDailyInventorySnapshotQuery getDailyInventorySnapshotQuery;
    private final GetProductMovementsQuery getProductMovementsQuery;

    @GetMapping("/daily-snapshot")
    public ResponseEntity<List<GetDailyInventorySnapshotQuery.SnapshotDto>> getDailySnapshot() {
        // Expected SLA: < 300ms
        List<GetDailyInventorySnapshotQuery.SnapshotDto> snapshot = getDailyInventorySnapshotQuery.execute();
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<GetProductMovementsQuery.MovementDto>> getProductMovements(@PathVariable UUID productId) {
        List<GetProductMovementsQuery.MovementDto> movements = getProductMovementsQuery.execute(productId);
        return ResponseEntity.ok(movements);
    }
}
