package com.restaurant.domain.strategy;

import com.restaurant.domain.model.PreparationMode;

import java.util.EnumMap;
import java.util.Map;

public class SubproductDeductionStrategyFactory {

    private static final Map<PreparationMode, SubproductDeductionStrategy> strategies = new EnumMap<>(PreparationMode.class);

    static {
        strategies.put(PreparationMode.BATCH, new BatchDeductionStrategy());
        strategies.put(PreparationMode.ON_THE_FLY, new OnTheFlyDeductionStrategy());
    }

    public static SubproductDeductionStrategy getStrategy(PreparationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Preparation mode must not be null");
        }
        SubproductDeductionStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new UnsupportedOperationException("No strategy configured for mode: " + mode);
        }
        return strategy;
    }
}
