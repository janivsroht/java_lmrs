package com.project.lmrs.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartnerMenuListingResponse {
    private String itemId;
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
