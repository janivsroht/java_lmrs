package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "housekeeping_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HousekeepingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "task_id", length = 36)
    private String taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "assigned_user_id", length = 36)
    private String assignedUserId;

    @Column(name = "task_type_enum", nullable = false, length = 50)
    private String taskType;

    @Column(name = "status_enum", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "priority_enum", nullable = false, length = 20)
    private String priority = "NORMAL";

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}