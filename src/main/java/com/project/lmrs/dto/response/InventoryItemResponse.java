package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryItemResponse {
    private String inventoryId;
    private String name;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal reorderThreshold;
    private BigDecimal costPerUnit;
}
