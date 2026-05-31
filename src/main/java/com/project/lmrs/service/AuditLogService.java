package com.project.lmrs.service;

import com.project.lmrs.entity.AuditLog;
import com.project.lmrs.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog log(String tenantId, String userId, String action,
                        String entityType, String entityId,
                        Map<String, Object> oldValue, Map<String, Object> newValue,
                        String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .build();

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditTrailByEntity(String tenantId, String entityType, String entityId) {
        return auditLogRepository.findAllByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId);
    }

    public List<AuditLog> getAuditTrailByUser(String userId, String tenantId) {
        return auditLogRepository.findAllByUserIdAndTenantId(userId, tenantId);
    }
}
