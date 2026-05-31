package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RestaurantTableResponse {
    private String tableId;
    private String tableNumber;
    private String zone;
    private int capacity;
    private String status;
    private BigDecimal positionX;
    private BigDecimal positionY;
}
