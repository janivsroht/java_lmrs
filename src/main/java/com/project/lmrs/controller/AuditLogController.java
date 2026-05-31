package com.project.lmrs.controller;

import com.project.lmrs.dto.response.AuditLogResponse;
import com.project.lmrs.entity.AuditLog;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditTrailByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(auditLogService.getAuditTrailByEntity(tenantId, entityType, entityId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditTrailByUser(@PathVariable String userId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(auditLogService.getAuditTrailByUser(userId, tenantId)
                .stream().map(this::toResponse).toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .logId(log.getLogId())
                .tenantId(log.getTenantId())
                .userId(log.getUserId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue() != null ? log.getOldValue().toString() : null)
                .newValue(log.getNewValue() != null ? log.getNewValue().toString() : null)
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
