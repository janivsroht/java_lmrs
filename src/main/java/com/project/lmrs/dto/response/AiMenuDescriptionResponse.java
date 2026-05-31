package com.project.lmrs.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiMenuDescriptionResponse {
    private String description;
    private String model;
    private long tokensUsed;
}
