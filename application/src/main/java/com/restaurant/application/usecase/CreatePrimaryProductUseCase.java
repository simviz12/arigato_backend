package com.restaurant.application.usecase;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.repository.PrimaryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.restaurant.application.dto.command.RegisterPurchaseCommand;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Weight;
import lombok.Data;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePrimaryProductUseCase {
    private final PrimaryProductRepository primaryProductRepository;
    private final RegisterPurchaseUseCase registerPurchaseUseCase;

    @Data
    public static class Command {
        private String name;
        private String unitOfMeasure;
        private Double minimumStockAlert;
        private UUID distributorId;
        private Double initialQuantityGrams;
        private Double initialPricePesos;
    }

    @Transactional
    public PrimaryProduct execute(Command command) {
        UnitOfMeasure uom = "Unidades".equalsIgnoreCase(command.getUnitOfMeasure()) ? UnitOfMeasure.UNIT : UnitOfMeasure.GRAM;
        
        PrimaryProduct product = PrimaryProduct.builder()
                .name(command.getName())
                .unitOfMeasure(uom)
                .minimumStockAlert(Weight.ofGrams(command.getMinimumStockAlert() != null ? command.getMinimumStockAlert() : 0.0))
                .preferredDistributorId(command.getDistributorId())
                .isResaleItem(false)
                .build();
                
        PrimaryProduct saved = primaryProductRepository.save(product);

        if (command.getInitialQuantityGrams() != null && command.getInitialQuantityGrams() > 0 && command.getDistributorId() != null) {
            RegisterPurchaseCommand purchaseCmd = new RegisterPurchaseCommand();
            purchaseCmd.setProductId(saved.getId());
            purchaseCmd.setDistributorId(command.getDistributorId());
            purchaseCmd.setQuantity(java.math.BigDecimal.valueOf(command.getInitialQuantityGrams()));
            purchaseCmd.setTotalPricePesos(java.math.BigDecimal.valueOf(command.getInitialPricePesos() != null ? command.getInitialPricePesos() : 0.0));
            registerPurchaseUseCase.execute(purchaseCmd);
        }

        return saved;
    }
}
