package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateOrderRequest;
import com.project.lmrs.dto.response.OrderResponse;
import com.project.lmrs.entity.*;
import com.project.lmrs.enums.OrderItemStatus;
import com.project.lmrs.enums.OrderStatus;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import com.project.lmrs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemVariantRepository menuItemVariantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final GuestRepository guestRepository;
    private final TenantRepository tenantRepository;
    private final AuditLogService auditLogService;

    public List<OrderResponse> getAllOrders(String tenantId) {
        return orderRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<OrderResponse> getAllOrdersPaged(String tenantId, Pageable pageable) {
        return orderRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<OrderResponse> getOrdersByStatus(String tenantId, String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        return orderRepository.findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, orderStatus)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String orderId, String tenantId) {
        Order order = orderRepository.findByOrderIdAndTenant_TenantIdAndIsDeletedFalse(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(String tenantId, CreateOrderRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(
                            request.getTableId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", request.getTableId()));
        }

        Guest guest = null;
        if (request.getGuestId() != null) {
            guest = guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(
                            request.getGuestId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", request.getGuestId()));
        }

        Order order = Order.builder()
                .tenant(tenant)
                .table(table)
                .serverUserId(request.getServerUserId())
                .guest(guest)
                .totalAmount(BigDecimal.ZERO)
                .isDeleted(false)
                .build();

        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findByItemIdAndTenant_TenantIdAndIsDeletedFalse(
                            itemReq.getMenuItemId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemReq.getMenuItemId()));

            MenuItemVariant variant = null;
            if (itemReq.getVariantId() != null) {
                variant = menuItemVariantRepository.findById(itemReq.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("MenuItemVariant", "id", itemReq.getVariantId()));
                if (!variant.getMenuItem().getItemId().equals(menuItem.getItemId())) {
                    throw new ResourceNotFoundException("MenuItemVariant", "id", itemReq.getVariantId());
                }
            }

            BigDecimal effectivePrice = menuItem.getBasePrice();
            if (variant != null) {
                effectivePrice = effectivePrice.add(variant.getPriceModifier());
            }

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(effectivePrice)
                    .modifiers(itemReq.getModifiers())
                    .isDeleted(false)
                    .build();

            items.add(item);
            total = total.add(effectivePrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        orderItemRepository.saveAll(items);
        order.setTotalAmount(total);
        orderRepository.save(order);

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String status, String tenantId) {
        Order order = orderRepository.findByOrderIdAndTenant_TenantIdAndIsDeletedFalse(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus newStatus = OrderStatus.valueOf(status);
        OrderStatus currentStatus = order.getStatus();

        // Validate transition
        Map<OrderStatus, List<OrderStatus>> allowedTransitions = Map.of(
                OrderStatus.OPEN, List.of(OrderStatus.SUBMITTED, OrderStatus.VOIDED),
                OrderStatus.SUBMITTED, List.of(OrderStatus.IN_KITCHEN, OrderStatus.VOIDED),
                OrderStatus.IN_KITCHEN, List.of(OrderStatus.READY, OrderStatus.VOIDED),
                OrderStatus.READY, List.of(OrderStatus.SERVED),
                OrderStatus.SERVED, List.of(OrderStatus.CLOSED)
        );

        List<OrderStatus> allowed = allowedTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.CLOSED) {
            order.setClosedAt(LocalDateTime.now());
        }

        order = orderRepository.save(order);

        // Audit log
        auditLogService.log(
                SecurityUtils.getCurrentTenantId(),
                SecurityUtils.getCurrentUserId(),
                "ORDER_STATUS_CHANGED",
                "Order",
                orderId,
                Map.of("oldStatus", currentStatus.name()),
                Map.of("newStatus", newStatus.name()),
                null
        );

        return toResponse(order);
    }

    @Transactional
    public void voidOrder(String orderId, String tenantId) {
        Order order = orderRepository.findByOrderIdAndTenant_TenantIdAndIsDeletedFalse(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new BusinessRuleException("Cannot void a closed order");
        }

        order.setStatus(OrderStatus.VOIDED);
        orderRepository.save(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItem> items = order.getItems() != null ? order.getItems() : List.of();

        List<OrderResponse.OrderItemDto> itemDtos = items.stream()
                .filter(i -> !i.isDeleted())
                .map(i -> OrderResponse.OrderItemDto.builder()
                        .orderItemId(i.getOrderItemId())
                        .menuItemName(i.getMenuItem().getName())
                        .variantName(i.getVariant() != null ? i.getVariant().getName() : null)
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .status(i.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .serverUserId(order.getServerUserId())
                .guestName(order.getGuest() != null
                        ? order.getGuest().getFirstName() + " " + order.getGuest().getLastName()
                        : null)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .openedAt(order.getOpenedAt())
                .closedAt(order.getClosedAt())
                .items(itemDtos)
                .build();
    }
}
