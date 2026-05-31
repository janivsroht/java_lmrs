package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class AiMenuDescriptionRequest {
    @NotBlank(message = "Item name is required")
    private String itemName;
    @NotBlank(message = "Category name is required")
    private String categoryName;
    private List<String> ingredients;
    private List<String> dietaryFlags;
    private double basePrice;
}
