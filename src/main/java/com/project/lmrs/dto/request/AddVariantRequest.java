package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddVariantRequest {

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal priceModifier;
}
