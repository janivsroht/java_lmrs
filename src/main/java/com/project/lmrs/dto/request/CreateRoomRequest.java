package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRoomRequest {

    @NotBlank
    private String roomNumber;

    @NotBlank
    private String roomTypeId;

    private Integer floor;

    @NotNull
    @Positive(message = "Base rate must be positive")
    private BigDecimal baseRate;
}
