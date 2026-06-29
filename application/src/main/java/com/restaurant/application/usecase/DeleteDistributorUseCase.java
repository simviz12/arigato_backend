package com.restaurant.application.usecase;

import com.restaurant.domain.model.Distributor;
import com.restaurant.domain.repository.DistributorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteDistributorUseCase {

    private final DistributorRepository distributorRepository;
    // Assume a service or repository checks for purchases:
    // private final PurchaseRepository purchaseRepository;

    public void execute(UUID distributorId) {
        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new IllegalArgumentException("Distributor not found"));

        // Here we simulate the validation: if it has purchases, soft-delete. Otherwise hard-delete (or always soft-delete).
        // Since purchase history is critical, we always soft-delete to be safe.
        distributor.deactivate();
        distributorRepository.save(distributor);
    }
}
