package com.restaurant.application.usecase;

import com.restaurant.domain.model.Distributor;
import com.restaurant.domain.repository.DistributorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateDistributorUseCase {
    private final DistributorRepository distributorRepository;

    @lombok.Data
    public static class Command {
        private String name;
        private String contactPhone;
        private String contactEmail;
        private String notes;
    }

    public Distributor execute(Command command) {
        Distributor distributor = Distributor.builder()
                .name(command.getName())
                .contactPhone(command.getContactPhone())
                .contactEmail(command.getContactEmail())
                .notes(command.getNotes())
                .active(true)
                .build();
        return distributorRepository.save(distributor);
    }
}
