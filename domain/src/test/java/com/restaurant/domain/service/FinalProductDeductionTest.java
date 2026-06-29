package com.restaurant.domain.service;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.SubproductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalProductDeductionTest {

    @Mock
    private PrimaryProductStockModifier stockModifier;

    @Mock
    private SubproductRepository subproductRepository;

    private FinalProductDeductionService service;

    private UUID ribsId = UUID.randomUUID();
    private UUID tomatoId = UUID.randomUUID();
    private UUID onionId = UUID.randomUUID();
    private UUID bbqSauceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new FinalProductDeductionService(stockModifier, subproductRepository);
    }

    @Test
    void deductsMixedRecipeCorrectly_CostillasBBQ_Scenario() {
        // SCENARIO: Sell 2 plates of "Costillas BBQ"
        // Plate = 200g Ribs (Primary) + 150g BBQ Sauce (Subproduct, ON_THE_FLY)
        
        // BBQ Sauce Recipe: yields 1000g, requires 800g Tomato + 200g Onion
        Subproduct bbqSauce = Subproduct.builder()
                .id(bbqSauceId)
                .name("BBQ Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.ON_THE_FLY)
                .build();
                
        List<SubproductIngredient> sauceRecipe = List.of(
                new SubproductIngredient(bbqSauceId, tomatoId, Weight.ofGrams(800)),
                new SubproductIngredient(bbqSauceId, onionId, Weight.ofGrams(200))
        );

        when(subproductRepository.findById(bbqSauceId)).thenReturn(Optional.of(bbqSauce));
        when(subproductRepository.findIngredientsBySubproductId(bbqSauceId)).thenReturn(sauceRecipe);

        List<FinalProductComponent> plateComponents = List.of(
                new FinalProductComponent(UUID.randomUUID(), new PrimaryComponentRef(ribsId), Weight.ofGrams(200)),
                new FinalProductComponent(UUID.randomUUID(), new SubproductComponentRef(bbqSauceId), Weight.ofGrams(150))
        );

        // Action: Sell 2 units
        service.deductForSale(plateComponents, 2);

        // Assertions for Primary Component
        // 2 units * 200g = 400g Ribs
        ArgumentCaptor<Weight> weightCaptor = ArgumentCaptor.forClass(Weight.class);
        verify(stockModifier).deductPrimaryProductStock(eq(ribsId), weightCaptor.capture());
        assertThat(weightCaptor.getValue().getGrams()).isEqualByComparingTo(new BigDecimal("400"));

        // Assertions for Subproduct Component
        // 2 units * 150g = 300g BBQ Sauce required.
        // Ratio = 300 / 1000 = 0.3
        // Tomato = 800 * 0.3 = 240g
        // Onion = 200 * 0.3 = 60g
        
        ArgumentCaptor<Weight> tomatoCaptor = ArgumentCaptor.forClass(Weight.class);
        verify(stockModifier).deductPrimaryProductStock(eq(tomatoId), tomatoCaptor.capture());
        assertThat(tomatoCaptor.getValue().getGrams()).isEqualByComparingTo(new BigDecimal("240"));

        ArgumentCaptor<Weight> onionCaptor = ArgumentCaptor.forClass(Weight.class);
        verify(stockModifier).deductPrimaryProductStock(eq(onionId), onionCaptor.capture());
        assertThat(onionCaptor.getValue().getGrams()).isEqualByComparingTo(new BigDecimal("60"));
        
        // Verify subproduct update is not called since it's ON_THE_FLY, not BATCH
        verify(subproductRepository, never()).update(any(), any());
    }
}
