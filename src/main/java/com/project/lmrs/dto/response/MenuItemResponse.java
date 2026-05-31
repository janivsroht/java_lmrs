package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItemResponse {
    private String itemId;
    private String categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private List<String> allergens;
    private List<String> dietaryFlags;
    private boolean isAvailable;
    private List<VariantDto> variants;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VariantDto {
        private String variantId;
        private String name;
        private BigDecimal priceModifier;
    }
}
