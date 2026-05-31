package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AvailabilityResponse {
    private String roomId;
    private String roomNumber;
    private String roomTypeName;
    private Integer floor;
    private BigDecimal baseRate;
    private List<String> amenities;
}
