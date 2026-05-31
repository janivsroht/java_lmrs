package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TableReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "table_res_id", length = 36)
    private String tableResId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "reservation_dt", nullable = false)
    private LocalDateTime reservationDt;

    @Column(name = "status_enum", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

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