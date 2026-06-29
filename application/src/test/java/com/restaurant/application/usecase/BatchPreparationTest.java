package com.restaurant.application.usecase;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.BatchPreparationLogRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.PrimaryProductStockModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchPreparationTest {

    @Mock
    private SubproductRepository subproductRepository;

    @Mock
    private PrimaryProductStockModifier stockModifier;

    @Mock
    private BatchPreparationLogRepository logRepository;

    @InjectMocks
    private PrepareBatchSubproductUseCase useCase;

    private UUID subproductId = UUID.randomUUID();
    private UUID tomatoId = UUID.randomUUID();

    private Subproduct sauce;
    private List<SubproductIngredient> recipe;

    @BeforeEach
    void setUp() {
        sauce = Subproduct.builder()
                .id(subproductId)
                .name("BBQ Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .build();

        recipe = List.of(
                new SubproductIngredient(subproductId, tomatoId, Weight.ofGrams(500))
        );
    }

    @Test
    void scalesProportionallyAndDeductsStock() {
        when(subproductRepository.findById(subproductId)).thenReturn(Optional.of(sauce));
        when(subproductRepository.findIngredientsBySubproductId(subproductId)).thenReturn(recipe);

        // We prepare 2000g, which is exactly double the 1000g yield.
        // Therefore, it should deduct 1000g of tomatoes (500g * 2).
        useCase.execute(subproductId, 2000.0, "CHEF_MARIO");

        verify(stockModifier).deductPrimaryProductStock(eq(tomatoId), argThat(weight -> weight.getGrams().compareTo(new java.math.BigDecimal("1000")) == 0));
        verify(logRepository).save(any());
        verify(subproductRepository).update(eq(sauce), eq(recipe));
    }

    @Test
    void rollsBackAtomicallyIfStockIsInsufficient() {
        when(subproductRepository.findById(subproductId)).thenReturn(Optional.of(sauce));
        when(subproductRepository.findIngredientsBySubproductId(subproductId)).thenReturn(recipe);

        // Simulate that the stock modifier throws an exception when attempting to deduct (insufficient stock)
        doThrow(new IllegalArgumentException("Not enough stock")).when(stockModifier).deductPrimaryProductStock(any(), any());

        assertThatThrownBy(() -> useCase.execute(subproductId, 2000.0, "CHEF_MARIO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough stock");

        // The transaction fails, so we never increment batch stock and never save logs.
        verify(logRepository, never()).save(any());
        verify(subproductRepository, never()).update(any(), any());
    }
}
