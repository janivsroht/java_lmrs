package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String tableNumber;
    private String serverUserId;
    private String guestName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private List<OrderItemDto> items;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemDto {
        private String orderItemId;
        private String menuItemName;
        private String variantName;
        private int quantity;
        private BigDecimal unitPrice;
        private String status;
    }
}
