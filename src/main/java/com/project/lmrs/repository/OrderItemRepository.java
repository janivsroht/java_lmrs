package com.project.lmrs.repository;

import com.project.lmrs.entity.OrderItem;
import com.project.lmrs.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrder_OrderIdAndIsDeletedFalse(String orderId);
    List<OrderItem> findAllByOrder_OrderIdAndStatusAndIsDeletedFalse(String orderId, OrderItemStatus status);

    @Query("SELECT oi.menuItem.name, SUM(oi.quantity) as qty FROM OrderItem oi " +
            "WHERE oi.order.tenant.tenantId = :tenantId AND oi.isDeleted = false " +
            "GROUP BY oi.menuItem.name ORDER BY qty DESC")
    List<Object[]> findTopMenuItems(@Param("tenantId") String tenantId);
}