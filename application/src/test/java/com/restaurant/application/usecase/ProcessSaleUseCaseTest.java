package com.restaurant.application.usecase;

import com.restaurant.domain.event.SaleCompletedEvent;
import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.repository.FinalProductRepository;
import com.restaurant.domain.repository.SaleRepository;
import com.restaurant.domain.service.FinalProductDeductionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessSaleUseCaseTest {

    @Mock
    private FinalProductRepository finalProductRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private FinalProductDeductionService deductionService;

    @Mock
    private GetFinalProductMarginUseCase getFinalProductMarginUseCase;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProcessSaleUseCase useCase;

    @Test
    void multiLineSaleRollsBackEntirelyIfOneItemFails() {
        // Mock a cart with 2 items. The 2nd item fails deduction.
        UUID dish1Id = UUID.randomUUID();
        UUID dish2Id = UUID.randomUUID();

        FinalProduct dish1 = FinalProduct.builder().id(dish1Id).name("Dish 1").sellingPrice(Money.ofPesos(100)).build();
        FinalProduct dish2 = FinalProduct.builder().id(dish2Id).name("Dish 2").sellingPrice(Money.ofPesos(200)).build();

        when(finalProductRepository.findById(dish1Id)).thenReturn(Optional.of(dish1));
        when(finalProductRepository.findById(dish2Id)).thenReturn(Optional.of(dish2));

        GetFinalProductMarginUseCase.MarginResponse margin = GetFinalProductMarginUseCase.MarginResponse.builder()
                .costPerUnit(new BigDecimal("50"))
                .sellingPrice(new BigDecimal("100"))
                .build();
        when(getFinalProductMarginUseCase.execute(any())).thenReturn(margin);

        List<FinalProductComponent> components1 = List.of();
        List<FinalProductComponent> components2 = List.of();
        when(finalProductRepository.findComponentsByProductId(dish1Id)).thenReturn(components1);
        when(finalProductRepository.findComponentsByProductId(dish2Id)).thenReturn(components2);

        // Make the deduction service throw for the second dish
        doThrow(new IllegalStateException("Not enough stock"))
                .when(deductionService).deductForSale(eq(components2), anyInt());

        ProcessSaleUseCase.Command cmd = ProcessSaleUseCase.Command.builder()
                .cashierId("Admin")
                .paymentMethod("CASH")
                .lines(List.of(
                        ProcessSaleUseCase.LineItemDto.builder().finalProductId(dish1Id).quantity(1).build(),
                        ProcessSaleUseCase.LineItemDto.builder().finalProductId(dish2Id).quantity(1).build()
                ))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");

        // Verify sale was NEVER saved because exception propagated (rollback)
        verify(saleRepository, never()).save(any(), any());
        
        // Verify event was NEVER published
        verify(eventPublisher, never()).publishEvent(any(SaleCompletedEvent.class));
    }

    @Test
    void saleCompletedEventIsFiredOnSuccess() {
        UUID dishId = UUID.randomUUID();
        FinalProduct dish = FinalProduct.builder().id(dishId).name("Dish 1").sellingPrice(Money.ofPesos(100)).build();

        when(finalProductRepository.findById(dishId)).thenReturn(Optional.of(dish));
        GetFinalProductMarginUseCase.MarginResponse margin = GetFinalProductMarginUseCase.MarginResponse.builder()
                .costPerUnit(new BigDecimal("50")).sellingPrice(new BigDecimal("100")).build();
        when(getFinalProductMarginUseCase.execute(dishId)).thenReturn(margin);

        ProcessSaleUseCase.Command cmd = ProcessSaleUseCase.Command.builder()
                .cashierId("Admin")
                .paymentMethod("CASH")
                .lines(List.of(ProcessSaleUseCase.LineItemDto.builder().finalProductId(dishId).quantity(1).build()))
                .build();

        useCase.execute(cmd);

        // Verify Event is fired
        verify(eventPublisher, times(1)).publishEvent(any(SaleCompletedEvent.class));
    }
}
