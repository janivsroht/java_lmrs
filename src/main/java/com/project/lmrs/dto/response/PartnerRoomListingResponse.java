package com.project.lmrs.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartnerRoomListingResponse {
    private String roomId;
    private String roomNumber;
    private String roomTypeName;
    private int floor;
    private int maxOccupancy;
    private String status;
    private BigDecimal baseRate;
    private List<String> amenities;
}
