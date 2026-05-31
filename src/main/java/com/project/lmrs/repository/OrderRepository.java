package com.project.lmrs.repository;

import com.project.lmrs.entity.Order;
import com.project.lmrs.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    List<Order> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    List<Order> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, OrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    Optional<Order> findByOrderIdAndIsDeletedFalse(String orderId);

    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    Optional<Order> findByOrderIdAndTenant_TenantIdAndIsDeletedFalse(String orderId, String tenantId);

    List<Order> findAllByTable_TableIdAndStatusAndIsDeletedFalse(String tableId, OrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    Page<Order> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.menuItem", "items.variant", "table", "guest"})
    Page<Order> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, OrderStatus status, Pageable pageable);
}
