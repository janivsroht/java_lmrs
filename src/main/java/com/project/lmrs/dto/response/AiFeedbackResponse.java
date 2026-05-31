package com.project.lmrs.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiFeedbackResponse {
    private String sentiment;
    private String summary;
    private String suggestedReply;
    private String model;
    private long tokensUsed;
}
