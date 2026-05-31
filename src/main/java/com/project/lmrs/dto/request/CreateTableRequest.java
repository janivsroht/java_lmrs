package com.project.lmrs.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTableRequest {

    @NotBlank
    private String tableNumber;

    private String zone;

    @NotNull
    @Min(1)
    private Integer capacity;

    private String status;

    private BigDecimal positionX;

    private BigDecimal positionY;
}
