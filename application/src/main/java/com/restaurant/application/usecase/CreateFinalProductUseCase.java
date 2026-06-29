package com.restaurant.application.usecase;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.FinalProductRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateFinalProductUseCase {

    private final FinalProductRepository repository;

    @Data
    public static class Command {
        private String name;
        private BigDecimal sellingPricePesos;
        private String category;
        private List<ComponentDto> components;
    }

    @Data
    public static class ComponentDto {
        private UUID primaryProductId;
        private UUID subproductId;
        private BigDecimal quantityGrams;
    }

    public UUID execute(Command command) {
        if (command.getComponents() == null || command.getComponents().isEmpty()) {
            throw new IllegalArgumentException("A final product must have at least one component");
        }

        FinalProduct product = FinalProduct.builder()
                .name(command.getName())
                .sellingPrice(Money.ofPesos(command.getSellingPricePesos().doubleValue()))
                .category(command.getCategory())
                .active(true)
                .build();

        List<FinalProductComponent> components = new ArrayList<>();
        
        for (ComponentDto dto : command.getComponents()) {
            ComponentReference ref;
            if (dto.getPrimaryProductId() != null && dto.getSubproductId() == null) {
                ref = new PrimaryComponentRef(dto.getPrimaryProductId());
            } else if (dto.getPrimaryProductId() == null && dto.getSubproductId() != null) {
                ref = new SubproductComponentRef(dto.getSubproductId());
            } else {
                throw new IllegalArgumentException("Component must reference EXACTLY ONE primary product OR subproduct");
            }
            
            components.add(new FinalProductComponent(product.getId(), ref, Weight.ofGrams(dto.getQuantityGrams())));
        }

        repository.save(product, components);
        return product.getId();
    }
}
