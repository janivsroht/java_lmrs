package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateHousekeepingTaskRequest;
import com.project.lmrs.dto.response.HousekeepingTaskResponse;
import com.project.lmrs.entity.HousekeepingTask;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.HousekeepingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/housekeeping")
@RequiredArgsConstructor
public class HousekeepingController {

    private final HousekeepingService housekeepingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','HOUSEKEEPER')")
    public ResponseEntity<List<HousekeepingTaskResponse>> getTasksByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(housekeepingService.getTasksByDate(tenantId, date)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('HOUSEKEEPER')")
    public ResponseEntity<List<HousekeepingTaskResponse>> getMyTasks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String userId = SecurityUtils.getCurrentUserId();
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(housekeepingService.getTasksByUser(userId, tenantId, date)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','HOUSEKEEPER')")
    public ResponseEntity<HousekeepingTaskResponse> getTaskById(@PathVariable String taskId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(housekeepingService.getTaskById(taskId, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<HousekeepingTaskResponse> createTask(@Valid @RequestBody CreateHousekeepingTaskRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(housekeepingService.createTask(tenantId, request)));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','HOUSEKEEPER')")
    public ResponseEntity<HousekeepingTaskResponse> updateTask(@PathVariable String taskId,
                                                       @Valid @RequestBody CreateHousekeepingTaskRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(housekeepingService.updateTask(taskId, request, tenantId)));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        housekeepingService.deleteTask(taskId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private HousekeepingTaskResponse toResponse(HousekeepingTask task) {
        HousekeepingTaskResponse.RoomBasic roomBasic = task.getRoom() != null
                ? HousekeepingTaskResponse.RoomBasic.builder()
                    .roomId(task.getRoom().getRoomId())
                    .roomNumber(task.getRoom().getRoomNumber())
                    .build()
                : null;
        return HousekeepingTaskResponse.builder()
                .taskId(task.getTaskId())
                .room(roomBasic)
                .taskType(task.getTaskType())
                .assignedUserId(task.getAssignedUserId())
                .priority(task.getPriority())
                .status(task.getStatus())
                .scheduledDate(task.getScheduledDate())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
