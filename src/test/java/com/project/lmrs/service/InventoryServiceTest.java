package com.project.lmrs.service;

import com.project.lmrs.dto.request.StockAdjustmentRequest;
import com.project.lmrs.entity.InventoryItem;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.InventoryItemRepository;
import com.project.lmrs.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private AuditLogService auditLogService;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryItemRepository, tenantRepository, auditLogService);
    }

    @Test
    void adjustStock_addStock_shouldSucceed() {
        String inventoryId = "inv1";
        String tenantId = "t1";
        String userId = "u1";

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        InventoryItem item = InventoryItem.builder()
                .inventoryId(inventoryId)
                .tenant(tenant)
                .name("Flour")
                .currentStock(BigDecimal.valueOf(10))
                .reorderThreshold(BigDecimal.valueOf(5))
                .unit("KG")
                .build();

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(5);
        request.setReason("Restock");

        when(inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId))
                .thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InventoryItem result = inventoryService.adjustStock(inventoryId, request, tenantId, userId);

        assertEquals(0, BigDecimal.valueOf(15).compareTo(result.getCurrentStock()));
        verify(auditLogService).log(any(), any(), eq("INVENTORY_STOCK_ADJUSTED"), any(), any(), any(), any(), any());
    }

    @Test
    void adjustStock_deductStock_shouldSucceed() {
        String inventoryId = "inv1";
        String tenantId = "t1";
        String userId = "u1";

        InventoryItem item = InventoryItem.builder()
                .inventoryId(inventoryId)
                .currentStock(BigDecimal.valueOf(20))
                .build();

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(-8);

        when(inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId))
                .thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InventoryItem result = inventoryService.adjustStock(inventoryId, request, tenantId, userId);

        assertEquals(0, BigDecimal.valueOf(12).compareTo(result.getCurrentStock()));
    }

    @Test
    void adjustStock_insufficientStock_shouldThrow() {
        String inventoryId = "inv1";
        String tenantId = "t1";
        String userId = "u1";

        InventoryItem item = InventoryItem.builder()
                .inventoryId(inventoryId)
                .currentStock(BigDecimal.valueOf(5))
                .build();

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(-10);

        when(inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse(inventoryId, tenantId))
                .thenReturn(Optional.of(item));

        assertThrows(BusinessRuleException.class,
                () -> inventoryService.adjustStock(inventoryId, request, tenantId, userId));
    }

    @Test
    void adjustStock_notFound_shouldThrow() {
        when(inventoryItemRepository.findByInventoryIdAndTenant_TenantIdAndIsDeletedFalse("bad", "t1"))
                .thenReturn(Optional.empty());

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(5);

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.adjustStock("bad", request, "t1", "u1"));
    }
}
