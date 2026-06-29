package com.restaurant.application.usecase;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.SubproductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeValidationTest {

    @Mock
    private SubproductRepository subproductRepository;

    @Mock
    private PrimaryProductRepository primaryProductRepository;

    @InjectMocks
    private CreateSubproductUseCase useCase;

    @Test
    void preventsUsingUnitBasedProductInRecipe() {
        UUID cokeId = UUID.randomUUID();
        PrimaryProduct coke = PrimaryProduct.builder()
                .id(cokeId)
                .name("Coca-Cola")
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .build();

        when(primaryProductRepository.findById(cokeId)).thenReturn(Optional.of(coke));

        CreateSubproductUseCase.CreateSubproductCommand command = new CreateSubproductUseCase.CreateSubproductCommand();
        command.setName("BBQ Sauce");
        command.setPreparationMode("BATCH");
        command.setTotalYieldGrams(1000.0);
        
        CreateSubproductUseCase.IngredientDto ingredient = new CreateSubproductUseCase.IngredientDto();
        ingredient.setPrimaryProductId(cokeId);
        ingredient.setQuantityGrams(50.0); // Attempting to use 50 grams of a bottled coke!
        command.setIngredients(List.of(ingredient));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot use a UNIT-based resale item");
    }
}
