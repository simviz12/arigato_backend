package com.restaurant.application.usecase;

import com.restaurant.domain.repository.FinalProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HardDeleteFinalProductUseCase {

    private final JdbcTemplate jdbcTemplate;

    // FOR TESTING/ADMIN PURPOSES ONLY. Bypasses soft-delete to trigger SQL constraints.
    public void execute(UUID id) {
        jdbcTemplate.update("DELETE FROM final_products WHERE id = ?", id);
    }
}
