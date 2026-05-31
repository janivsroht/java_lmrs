package com.project.lmrs.repository;

import com.project.lmrs.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    @Query("SELECT i FROM InventoryItem i WHERE i.tenant.tenantId = :tenantId " +
            "AND i.currentStock <= i.reorderThreshold AND i.isDeleted = false")
    List<InventoryItem> findLowStockItems(@Param("tenantId") String tenantId);

    Optional<InventoryItem> findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(String inventoryId, String tenantId);
}
