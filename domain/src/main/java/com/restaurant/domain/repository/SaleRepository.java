package com.restaurant.domain.repository;

import com.restaurant.domain.model.Sale;
import com.restaurant.domain.model.SaleLine;

import java.util.List;

public interface SaleRepository {
    void save(Sale sale, List<SaleLine> lines);
}
