package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StockAdjustmentRequest {

    @NotNull
    private Integer quantity;

    private String reason;
}
