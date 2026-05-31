package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomResponse {
    private String roomId;
    private String roomNumber;
    private String roomTypeId;
    private String roomTypeName;
    private Integer floor;
    private String status;
    private String housekeepingStatus;
    private BigDecimal baseRate;
}
