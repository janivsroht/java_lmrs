package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PostChargeRequest {

    @NotBlank
    private String description;

    @NotNull
    @Positive(message = "Charge amount must be positive")
    private BigDecimal amount;

    @NotBlank
    private String chargeType;
}
