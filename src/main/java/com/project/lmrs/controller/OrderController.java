package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateOrderRequest;
import com.project.lmrs.dto.response.OrderResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(orderService.getAllOrders(tenantId));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER')")
    public ResponseEntity<Page<OrderResponse>> getOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "openedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(orderService.getAllOrdersPaged(tenantId, pageRequest));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(orderService.getOrdersByStatus(tenantId, status));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(orderService.getOrderById(orderId, tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(tenantId, request));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','KITCHEN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String orderId,
                                                           @RequestParam String status) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, tenantId));
    }

    @PutMapping("/{orderId}/void")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> voidOrder(@PathVariable String orderId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        orderService.voidOrder(orderId, tenantId);
        return ResponseEntity.noContent().build();
    }
}
