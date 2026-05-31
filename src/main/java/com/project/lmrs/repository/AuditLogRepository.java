package com.project.lmrs.repository;

import com.project.lmrs.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findAllByTenantIdAndEntityTypeAndEntityId(String tenantId, String entityType, String entityId);
    List<AuditLog> findAllByUserId(String userId);
    List<AuditLog> findAllByUserIdAndTenantId(String userId, String tenantId);
}