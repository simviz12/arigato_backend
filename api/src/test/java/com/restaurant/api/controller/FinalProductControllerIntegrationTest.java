package com.restaurant.api.controller;

import com.restaurant.application.usecase.CreateFinalProductUseCase;
import com.restaurant.application.usecase.GetFinalProductMarginUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FinalProductController.class)
class FinalProductControllerIntegrationTest {

    @MockBean
    private CreateFinalProductUseCase createFinalProductUseCase;

    @MockBean
    private com.restaurant.application.usecase.DeleteFinalProductUseCase deleteFinalProductUseCase;

    @MockBean
    private GetFinalProductMarginUseCase getFinalProductMarginUseCase;

    @Autowired
    private FinalProductController controller;

    @Test
    void createProduct_ReturnsCreatedId() {
        UUID expectedId = UUID.randomUUID();
        when(createFinalProductUseCase.execute(any())).thenReturn(expectedId);

        CreateFinalProductUseCase.Command cmd = new CreateFinalProductUseCase.Command();
        cmd.setName("Costillas BBQ");
        cmd.setSellingPricePesos(new BigDecimal("35000"));
        
        ResponseEntity<UUID> response = controller.createProduct(cmd);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedId);
    }

    @Test
    void getMargin_ReturnsCalculatedMetrics() {
        UUID productId = UUID.randomUUID();
        
        GetFinalProductMarginUseCase.MarginResponse mockResponse = GetFinalProductMarginUseCase.MarginResponse.builder()
                .costPerUnit(new BigDecimal("12500.00"))
                .sellingPrice(new BigDecimal("35000.00"))
                .marginAmount(new BigDecimal("22500.00"))
                .marginPercentage(new BigDecimal("64.29"))
                .build();
                
        when(getFinalProductMarginUseCase.execute(productId)).thenReturn(mockResponse);

        ResponseEntity<GetFinalProductMarginUseCase.MarginResponse> response = controller.getMargin(productId);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMarginPercentage()).isEqualByComparingTo(new BigDecimal("64.29"));
    }
}
