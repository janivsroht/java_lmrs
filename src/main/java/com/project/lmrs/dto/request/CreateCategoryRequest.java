package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    private int displayOrder;

    private boolean isActive = true;
}
