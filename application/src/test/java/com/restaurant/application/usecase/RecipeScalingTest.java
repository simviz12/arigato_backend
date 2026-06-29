package com.restaurant.application.usecase;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.BatchPreparationLogRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.PrimaryProductStockModifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeScalingTest {

    @Mock
    private SubproductRepository subproductRepository;

    @Mock
    private PrimaryProductStockModifier stockModifier;

    @Mock
    private BatchPreparationLogRepository logRepository;

    @InjectMocks
    private PrepareBatchSubproductUseCase useCase;

    @Test
    void scalesComplexFractionsWithAbsolutePrecision() {
        UUID subproductId = UUID.randomUUID();
        UUID saltId = UUID.randomUUID();

        // Base recipe: 1200g of sauce requires exactly 15g of salt
        Subproduct sauce = Subproduct.builder()
                .id(subproductId)
                .name("Sauce")
                .totalYield(Weight.ofGrams(1200))
                .preparationMode(PreparationMode.BATCH)
                .build();

        List<SubproductIngredient> recipe = List.of(
                new SubproductIngredient(subproductId, saltId, Weight.ofGrams(15))
        );

        when(subproductRepository.findById(subproductId)).thenReturn(Optional.of(sauce));
        when(subproductRepository.findIngredientsBySubproductId(subproductId)).thenReturn(recipe);

        // Action: Cook prepares a weird batch size of 1750g
        // Mathematical Ratio: 1750 / 1200 = 1.458333...
        // Expected Salt: 15 * 1.458333333 = 21.875
        useCase.execute(subproductId, 1750.0, "ADMIN");

        ArgumentCaptor<Weight> weightCaptor = ArgumentCaptor.forClass(Weight.class);
        verify(stockModifier).deductPrimaryProductStock(eq(saltId), weightCaptor.capture());

        Weight deducted = weightCaptor.getValue();
        
        // Assert precision up to 6 decimals (21.875000)
        assertThat(deducted.getGrams()).isEqualByComparingTo(new BigDecimal("21.875000"));
    }
}
