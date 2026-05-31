package com.project.lmrs.repository;

import com.project.lmrs.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, String> {
    List<RestaurantTable> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<RestaurantTable> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, String status);
    Optional<RestaurantTable> findByTableIdAndTenant_TenantIdAndIsDeletedFalse(String tableId, String tenantId);
}
