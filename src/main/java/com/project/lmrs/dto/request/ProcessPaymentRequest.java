package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessPaymentRequest {

    @NotNull
    @Positive(message = "Payment amount must be positive")
    private BigDecimal amount;

    @NotBlank
    private String method;

    private String gatewayRef;
}
