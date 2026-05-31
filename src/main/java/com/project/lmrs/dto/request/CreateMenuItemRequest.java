package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateMenuItemRequest {

    @NotBlank
    private String categoryId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal basePrice;

    private List<String> allergens;

    private List<String> dietaryFlags;

    private Boolean isAvailable;
}
