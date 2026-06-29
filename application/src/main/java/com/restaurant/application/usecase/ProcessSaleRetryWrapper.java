package com.restaurant.application.usecase;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSaleRetryWrapper {

    private final ProcessSaleUseCase processSaleUseCase;
    private final MeterRegistry meterRegistry;
    
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 50;

    /**
     * Attempts to process a sale, automatically retrying if an OptimisticLockingFailureException occurs.
     * This wrapper ensures the retries happen outside the @Transactional boundary so a fresh transaction
     * is started on each attempt.
     */
    public UUID executeWithRetry(ProcessSaleUseCase.Command command) {
        int attempt = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while (true) {
            try {
                attempt++;
                return processSaleUseCase.execute(command);
                
            } catch (OptimisticLockingFailureException ex) {
                meterRegistry.counter("inventory.lock.retries", "attempt", String.valueOf(attempt)).increment();
                
                if (attempt >= MAX_RETRIES) {
                    log.error("Sale failed after {} concurrent attempts due to high contention.", MAX_RETRIES);
                    throw new IllegalStateException("El sistema está procesando muchas ventas de este producto. Por favor intente de nuevo.", ex);
                }
                
                log.warn("Concurrent modification detected on attempt {}. Retrying in {}ms...", attempt, backoff);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Retry interrupted", ie);
                }
                
                // Exponential backoff
                backoff *= 2;
            }
        }
    }
}
