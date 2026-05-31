package com.project.lmrs.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class RoomTypeResponse {
    private String roomTypeId;
    private String name;
    private int maxOccupancy;
    private String description;
    private List<String> amenities;
}
