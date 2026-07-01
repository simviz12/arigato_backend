package com.restaurant.application.config;

import com.restaurant.domain.port.AlertPort;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import com.restaurant.domain.service.PurchaseRegistrationService;
import com.restaurant.domain.service.PrimaryProductStockModifier;
import com.restaurant.domain.model.PrimaryProduct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public PurchaseRegistrationService purchaseRegistrationService(
            PurchaseRepository purchaseRepository,
            PrimaryProductRepository primaryProductRepository) {
        return new PurchaseRegistrationService(purchaseRepository, primaryProductRepository);
    }

    @Bean
    public PrimaryProductStockModifier primaryProductStockModifier(
            PrimaryProductRepository primaryProductRepository,
            AlertPort alertPort) {
        return (primaryProductId, quantityGrams) -> {
            PrimaryProduct product = primaryProductRepository.findById(primaryProductId)
                    .orElseThrow(() -> new IllegalArgumentException("Primary product not found: " + primaryProductId));
            
            // This method naturally throws if stock goes below zero
            product.removeStock(quantityGrams);
            primaryProductRepository.save(product);

            // After deduction, check if stock has fallen to critical level and send alert
            if (product.isStockLow()) {
                String msg = "⚠️ ¡Stock Crítico! El producto \"" + product.getName()
                        + "\" está por agotarse. Stock restante: " + product.getCurrentStock().getGrams().toPlainString() + " g.";
                alertPort.sendInventoryAlert(msg);
            }
        };
    }
}
