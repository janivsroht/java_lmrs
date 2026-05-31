package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateInventoryItemRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String unit;

    @NotNull
    @PositiveOrZero
    private BigDecimal currentStock;

    @NotNull
    @PositiveOrZero
    private BigDecimal reorderThreshold;

    @NotNull
    @PositiveOrZero
    private BigDecimal costPerUnit;
}
