package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryResponse {
    private String categoryId;
    private String name;
    private int displayOrder;
    private boolean isActive;
}
