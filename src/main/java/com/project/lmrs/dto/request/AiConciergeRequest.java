package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiConciergeRequest {
    @NotBlank(message = "Query is required")
    private String query;
    private String guestId;
    private String reservationId;
    private String roomId;
}
