package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateInventoryItemRequest;
import com.project.lmrs.dto.request.StockAdjustmentRequest;
import com.project.lmrs.entity.InventoryItem;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.security.SecurityUtils;
import java.math.BigDecimal;
import com.project.lmrs.repository.InventoryItemRepository;
import com.project.lmrs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final TenantRepository tenantRepository;
    private final AuditLogService auditLogService;

    public List<InventoryItem> getAllInventoryItems(String tenantId) {
        return inventoryItemRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId);
    }

    public List<InventoryItem> getLowStockItems(String tenantId) {
        return inventoryItemRepository.findLowStockItems(tenantId);
    }

    public InventoryItem getInventoryItemById(String inventoryId, String tenantId) {
        return inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", inventoryId));
    }

    @Transactional
    public InventoryItem createInventoryItem(String tenantId, CreateInventoryItemRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        InventoryItem item = InventoryItem.builder()
                .tenant(tenant)
                .name(request.getName())
                .unit(request.getUnit())
                .currentStock(request.getCurrentStock())
                .reorderThreshold(request.getReorderThreshold())
                .costPerUnit(request.getCostPerUnit())
                .isDeleted(false)
                .build();

        item = inventoryItemRepository.save(item);

        auditLogService.log(
                tenantId, SecurityUtils.getCurrentUserId(),
                "INVENTORY_ITEM_CREATED",
                "InventoryItem", item.getInventoryId(),
                Map.of(), Map.of("name", item.getName(), "stock", item.getCurrentStock()),
                null
        );

        return item;
    }

    @Transactional
    public InventoryItem updateInventoryItem(String inventoryId, CreateInventoryItemRequest request, String tenantId) {
        InventoryItem item = inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", inventoryId));

        String oldName = item.getName();
        BigDecimal oldStock = item.getCurrentStock();

        item.setName(request.getName());
        item.setUnit(request.getUnit());
        item.setCurrentStock(request.getCurrentStock());
        item.setReorderThreshold(request.getReorderThreshold());
        item.setCostPerUnit(request.getCostPerUnit());

        item = inventoryItemRepository.save(item);

        auditLogService.log(
                tenantId, SecurityUtils.getCurrentUserId(),
                "INVENTORY_ITEM_UPDATED",
                "InventoryItem", inventoryId,
                Map.of("oldName", oldName, "oldStock", oldStock),
                Map.of("newName", item.getName(), "newStock", item.getCurrentStock()),
                null
        );

        return item;
    }

    @Transactional
    public InventoryItem adjustStock(String inventoryId, StockAdjustmentRequest request, String tenantId,
                                      String userId) {
        InventoryItem item = inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", inventoryId));

        BigDecimal oldStock = item.getCurrentStock();
        BigDecimal adjustment = BigDecimal.valueOf(request.getQuantity());
        BigDecimal newStock = oldStock.add(adjustment);

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Insufficient stock. Available: " + oldStock
                    + ", requested adjustment: " + request.getQuantity());
        }

        item.setCurrentStock(newStock);
        item = inventoryItemRepository.save(item);

        auditLogService.log(
                tenantId, userId,
                "INVENTORY_STOCK_ADJUSTED",
                "InventoryItem",
                inventoryId,
                Map.of("oldStock", oldStock, "reason", request.getReason() != null ? request.getReason() : ""),
                Map.of("newStock", newStock, "adjustment", request.getQuantity()),
                null
        );

        return item;
    }

    @Transactional
    public void deleteInventoryItem(String inventoryId, String tenantId) {
        InventoryItem item = inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", inventoryId));
        String name = item.getName();
        item.setDeleted(true);
        inventoryItemRepository.save(item);

        auditLogService.log(
                tenantId, SecurityUtils.getCurrentUserId(),
                "INVENTORY_ITEM_DELETED",
                "InventoryItem", inventoryId,
                Map.of("name", name),
                Map.of(),
                null
        );
    }
}
