package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiFeedbackRequest {
    @NotBlank(message = "Feedback text is required")
    private String feedbackText;
    private String guestName;
}
