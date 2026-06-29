package com.restaurant.api.config;

import com.restaurant.domain.repository.*;
import com.restaurant.domain.service.*;
import com.restaurant.domain.model.vo.Weight;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DomainConfig {

    @Bean
    public PricePerUnitCalculator pricePerUnitCalculator() {
        return new PricePerUnitCalculator();
    }

    @Bean
    public SubproductCostCalculator subproductCostCalculator() {
        return new SubproductCostCalculator();
    }

    @Bean
    public FinalProductCostCalculator finalProductCostCalculator() {
        return new FinalProductCostCalculator();
    }

    @Bean
    public FinalProductDeductionService finalProductDeductionService(
            PrimaryProductStockModifier stockModifier,
            SubproductRepository subproductRepository) {
        return new FinalProductDeductionService(stockModifier, subproductRepository);
    }
}
