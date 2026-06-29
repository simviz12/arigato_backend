package com.restaurant.application.usecase;

import com.restaurant.application.usecase.GetFinalProductMarginUseCase;
import com.restaurant.domain.event.SaleCompletedEvent;
import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PaymentMethod;
import com.restaurant.domain.model.Sale;
import com.restaurant.domain.model.SaleLine;
import com.restaurant.domain.model.SaleStatus;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.repository.FinalProductRepository;
import com.restaurant.domain.repository.SaleRepository;
import com.restaurant.domain.service.FinalProductDeductionService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessSaleUseCase {

    private final FinalProductRepository finalProductRepository;
    private final SaleRepository saleRepository;
    private final FinalProductDeductionService deductionService;
    private final GetFinalProductMarginUseCase getFinalProductMarginUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @Data
    @Builder
    public static class Command {
        private String cashierId;
        private BigDecimal cashAmountPesos;
        private BigDecimal nequiAmountPesos;
        private List<LineItemDto> lines;
        private BigDecimal discountPesos;
    }

    @Data
    @Builder
    public static class LineItemDto {
        private UUID finalProductId;
        private Integer quantity;
    }

    /**
     * Executes the sale atomically. If any component deduction fails (due to insufficient stock),
     * the entire transaction rolls back via Spring's transactional management.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID execute(Command command) {
        if (command.getLines() == null || command.getLines().isEmpty()) {
            throw new IllegalArgumentException("Sale must have at least one line item");
        }

        UUID saleId = UUID.randomUUID();
        List<SaleLine> saleLines = new ArrayList<>();
        BigDecimal totalPesos = BigDecimal.ZERO;

        for (LineItemDto line : command.getLines()) {
            FinalProduct product = finalProductRepository.findById(line.getFinalProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Final product not found: " + line.getFinalProductId()));
            
            if (!product.isActive()) {
                throw new IllegalStateException("Cannot sell inactive product: " + product.getName());
            }

            // 1. Snapshot the cost exactly at the moment of sale
            GetFinalProductMarginUseCase.MarginResponse margin = getFinalProductMarginUseCase.execute(product.getId());
            Money unitCost = Money.ofPesos(margin.getCostPerUnit().doubleValue());
            
            // 2. Compute sale price
            Money unitPrice = product.getSellingPrice();
            totalPesos = totalPesos.add(unitPrice.getPesos().multiply(new BigDecimal(line.getQuantity())));

            // 3. Create the historical snapshot line
            saleLines.add(new SaleLine(
                    saleId, 
                    product.getId(), 
                    line.getQuantity(), 
                    unitPrice, 
                    unitCost
            ));

            // 4. Delegate to deduction service for recursive stock deduction
            List<FinalProductComponent> components = finalProductRepository.findComponentsByProductId(product.getId());
            deductionService.deductForSale(components, line.getQuantity());
        }

        // Apply discount if any
        BigDecimal discount = command.getDiscountPesos() != null ? command.getDiscountPesos() : BigDecimal.ZERO;
        totalPesos = totalPesos.subtract(discount);
        if (totalPesos.compareTo(BigDecimal.ZERO) < 0) totalPesos = BigDecimal.ZERO;

        BigDecimal cashAmount = command.getCashAmountPesos() != null ? command.getCashAmountPesos() : BigDecimal.ZERO;
        BigDecimal nequiAmount = command.getNequiAmountPesos() != null ? command.getNequiAmountPesos() : BigDecimal.ZERO;

        PaymentMethod calculatedPaymentMethod;
        if (cashAmount.compareTo(BigDecimal.ZERO) > 0 && nequiAmount.compareTo(BigDecimal.ZERO) > 0) {
            calculatedPaymentMethod = PaymentMethod.MIXED;
        } else if (nequiAmount.compareTo(BigDecimal.ZERO) > 0) {
            calculatedPaymentMethod = PaymentMethod.NEQUI;
        } else {
            calculatedPaymentMethod = PaymentMethod.CASH;
        }

        Long cashCents = cashAmount.multiply(new BigDecimal("100")).longValue();
        Long nequiCents = nequiAmount.multiply(new BigDecimal("100")).longValue();

        Sale sale = Sale.builder()
                .id(saleId)
                .cashierId(command.getCashierId())
                .paymentMethod(calculatedPaymentMethod)
                .totalAmount(Money.ofPesos(totalPesos.doubleValue()))
                .discount(Money.ofPesos(discount.doubleValue()))
                .cashAmountCents(cashCents)
                .nequiAmountCents(nequiCents)
                .status(SaleStatus.COMPLETED)
                .build();

        // 5. Persist Sale
        saleRepository.save(sale, saleLines);

        // 6. Publish Event Asynchronously (non-blocking)
        eventPublisher.publishEvent(new SaleCompletedEvent(this, sale, saleLines));

        return saleId;
    }
}
