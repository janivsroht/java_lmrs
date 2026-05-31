package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", length = 36)
    private String logId;

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 36)
    private String entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value_json", columnDefinition = "JSON")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value_json", columnDefinition = "JSON")
    private Map<String, Object> newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}