package com.project.lmrs.repository;

import com.project.lmrs.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {
    List<MenuCategory> findAllByTenant_TenantIdAndIsDeletedFalseOrderByDisplayOrderAsc(String tenantId);
    List<MenuCategory> findAllByTenant_TenantIdAndIsActiveTrueAndIsDeletedFalse(String tenantId);
    Optional<MenuCategory> findByCategoryIdAndTenant_TenantIdAndIsDeletedFalse(String categoryId, String tenantId);
    MenuCategory findByNameAndTenant_TenantId(String name, String tenantId);
}