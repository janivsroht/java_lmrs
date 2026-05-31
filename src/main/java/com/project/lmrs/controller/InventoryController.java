package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateInventoryItemRequest;
import com.project.lmrs.dto.request.StockAdjustmentRequest;
import com.project.lmrs.dto.response.InventoryItemResponse;
import com.project.lmrs.entity.InventoryItem;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<List<InventoryItemResponse>> getAllInventoryItems() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getAllInventoryItems(tenantId).stream().map(this::toResponse).toList());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getLowStockItems(tenantId).stream().map(this::toResponse).toList());
    }

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<InventoryItemResponse> getInventoryItemById(@PathVariable String inventoryId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(inventoryService.getInventoryItemById(inventoryId, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(inventoryService.createInventoryItem(tenantId, request)));
    }

    @PutMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(@PathVariable String inventoryId,
                                                                      @Valid @RequestBody CreateInventoryItemRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(inventoryService.updateInventoryItem(inventoryId, request, tenantId)));
    }

    @PutMapping("/{inventoryId}/stock")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InventoryItemResponse> adjustStock(@PathVariable String inventoryId,
                                                              @Valid @RequestBody StockAdjustmentRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(toResponse(inventoryService.adjustStock(inventoryId, request, tenantId, userId)));
    }

    @DeleteMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String inventoryId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        inventoryService.deleteInventoryItem(inventoryId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .inventoryId(item.getInventoryId())
                .name(item.getName())
                .unit(item.getUnit())
                .currentStock(item.getCurrentStock())
                .reorderThreshold(item.getReorderThreshold())
                .costPerUnit(item.getCostPerUnit())
                .build();
    }
}
