package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateHousekeepingTaskRequest {

    @NotBlank
    private String roomId;

    @NotBlank
    private String taskType;

    private String status;

    private String priority;

    private String assignedUserId;

    @NotNull
    private LocalDate scheduledDate;
}
