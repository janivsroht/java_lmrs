package com.project.lmrs.repository;

import com.project.lmrs.entity.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    @EntityGraph(attributePaths = {"variants", "category"})
    List<MenuItem> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    List<MenuItem> findAllByCategory_CategoryIdAndIsDeletedFalse(String categoryId);

    @EntityGraph(attributePaths = {"variants", "category"})
    List<MenuItem> findAllByTenant_TenantIdAndIsAvailableTrueAndIsDeletedFalse(String tenantId);

    @EntityGraph(attributePaths = {"variants", "category"})
    Optional<MenuItem> findByItemIdAndIsDeletedFalse(String itemId);

    @EntityGraph(attributePaths = {"variants", "category"})
    Optional<MenuItem> findByItemIdAndTenant_TenantIdAndIsDeletedFalse(String itemId, String tenantId);
}