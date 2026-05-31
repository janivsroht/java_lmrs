package com.project.lmrs.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiConciergeResponse {
    private String reply;
    private String model;
    private long tokensUsed;
}
