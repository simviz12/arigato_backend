package com.restaurant.domain.repository;

import com.restaurant.domain.model.BatchPreparationLog;

import java.util.List;
import java.util.UUID;

public interface BatchPreparationLogRepository {
    BatchPreparationLog save(BatchPreparationLog log);
    List<BatchPreparationLog> findBySubproductId(UUID subproductId);
}
