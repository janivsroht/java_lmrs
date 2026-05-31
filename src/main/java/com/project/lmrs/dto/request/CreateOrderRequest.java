package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CreateOrderRequest {

    private String tableId;

    private String serverUserId;

    private String guestId;

    @NotNull
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {

        @NotBlank
        private String menuItemId;

        private String variantId;

        @NotNull
        private int quantity;

        @NotNull
        private BigDecimal unitPrice;

        private Map<String, Object> modifiers;
    }
}
