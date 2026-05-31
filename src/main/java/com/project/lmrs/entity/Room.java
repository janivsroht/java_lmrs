package com.project.lmrs.entity;

import com.project.lmrs.enums.HousekeepingStatus;
import com.project.lmrs.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_id", length = 36)
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_enum", nullable = false, length = 30)
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "housekeeping_status_enum", nullable = false, length = 30)
    @Builder.Default
    private HousekeepingStatus housekeepingStatus = HousekeepingStatus.CLEAN;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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