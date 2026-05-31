package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateHousekeepingTaskRequest;
import com.project.lmrs.entity.HousekeepingTask;
import com.project.lmrs.entity.Room;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.HousekeepingTaskRepository;
import com.project.lmrs.repository.RoomRepository;
import com.project.lmrs.repository.TenantRepository;
import com.project.lmrs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HousekeepingService {

    private final HousekeepingTaskRepository housekeepingTaskRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final AuditLogService auditLogService;

    public List<HousekeepingTask> getTasksByDate(String tenantId, LocalDate date) {
        return housekeepingTaskRepository.findAllByTenant_TenantIdAndScheduledDateAndIsDeletedFalse(tenantId, date);
    }

    public List<HousekeepingTask> getTasksByUser(String userId, String tenantId, LocalDate date) {
        return housekeepingTaskRepository.findAllByAssignedUserIdAndTenant_TenantIdAndScheduledDateAndIsDeletedFalse(userId, tenantId, date);
    }

    public HousekeepingTask getTaskById(String taskId, String tenantId) {
        return housekeepingTaskRepository.findByTaskIdAndTenant_TenantIdAndIsDeletedFalse(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("HousekeepingTask", "id", taskId));
    }

    @Transactional
    public HousekeepingTask createTask(String tenantId, CreateHousekeepingTaskRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Room room = roomRepository.findById(request.getRoomId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        HousekeepingTask task = HousekeepingTask.builder()
                .tenant(tenant)
                .room(room)
                .taskType(request.getTaskType())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .assignedUserId(request.getAssignedUserId())
                .scheduledDate(request.getScheduledDate())
                .isDeleted(false)
                .build();

        return housekeepingTaskRepository.save(task);
    }

    @Transactional
    public HousekeepingTask updateTask(String taskId, CreateHousekeepingTaskRequest request, String tenantId) {
        HousekeepingTask task = housekeepingTaskRepository.findByTaskIdAndTenant_TenantIdAndIsDeletedFalse(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("HousekeepingTask", "id", taskId));

        String oldStatus = task.getStatus();

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .filter(r -> !r.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
            task.setRoom(room);
        }

        task.setTaskType(request.getTaskType());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setScheduledDate(request.getScheduledDate());

        if ("COMPLETED".equals(request.getStatus()) && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        }

        task = housekeepingTaskRepository.save(task);

        // Audit log
        auditLogService.log(
                SecurityUtils.getCurrentTenantId(),
                SecurityUtils.getCurrentUserId(),
                "HOUSEKEEPING_TASK_UPDATED",
                "HousekeepingTask",
                taskId,
                Map.of("oldStatus", oldStatus != null ? oldStatus : ""),
                Map.of("newStatus", task.getStatus() != null ? task.getStatus() : "",
                        "assignedUserId", task.getAssignedUserId() != null ? task.getAssignedUserId() : ""),
                null
        );

        return task;
    }

    @Transactional
    public void deleteTask(String taskId, String tenantId) {
        HousekeepingTask task = housekeepingTaskRepository.findByTaskIdAndTenant_TenantIdAndIsDeletedFalse(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("HousekeepingTask", "id", taskId));
        task.setDeleted(true);
        housekeepingTaskRepository.save(task);
    }
}
