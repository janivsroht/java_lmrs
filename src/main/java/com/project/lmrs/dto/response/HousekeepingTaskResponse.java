package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HousekeepingTaskResponse {
    private String taskId;
    private RoomBasic room;
    private String taskType;
    private String assignedUserId;
    private String priority;
    private String status;
    private LocalDate scheduledDate;
    private LocalDateTime completedAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RoomBasic {
        private String roomId;
        private String roomNumber;
    }
}
