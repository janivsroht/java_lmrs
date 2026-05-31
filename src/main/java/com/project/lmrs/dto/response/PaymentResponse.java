package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private BigDecimal amount;
    private String method;
    private String status;
    private String gatewayRef;
    private LocalDateTime paidAt;
}
