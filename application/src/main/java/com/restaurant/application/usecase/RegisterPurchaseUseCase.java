package com.restaurant.application.usecase;

import com.restaurant.application.dto.command.RegisterPurchaseCommand;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.PurchaseRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterPurchaseUseCase {

    private final PurchaseRegistrationService purchaseRegistrationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void execute(RegisterPurchaseCommand command) {
        purchaseRegistrationService.registerPurchase(
                command.getProductId(),
                command.getDistributorId(),
                Weight.ofGrams(command.getQuantity()),
                Money.ofPesos(command.getTotalPricePesos().doubleValue())
        );
    }
}
