package com.restaurant.domain.service;

import com.restaurant.domain.model.vo.Weight;
import java.util.UUID;

public interface PrimaryProductStockModifier {
    void deductPrimaryProductStock(UUID primaryProductId, Weight quantityGrams);
}
