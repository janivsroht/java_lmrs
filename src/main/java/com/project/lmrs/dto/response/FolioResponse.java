package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FolioResponse {
    private String folioId;
    private String reservationId;
    private String guestName;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private List<LineItemDto> lineItems;
    private List<PaymentDto> payments;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LineItemDto {
        private String lineItemId;
        private String description;
        private BigDecimal amount;
        private String chargeType;
        private LocalDateTime postedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentDto {
        private String paymentId;
        private BigDecimal amount;
        private String method;
        private String status;
        private LocalDateTime paidAt;
    }
}
