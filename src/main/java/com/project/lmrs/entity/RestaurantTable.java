package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "table_id", length = 36)
    private String tableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "table_number", nullable = false, length = 20)
    private String tableNumber;

    @Column(name = "zone_enum", length = 30)
    private String zone;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "status_enum", nullable = false, length = 30)
    private String status = "AVAILABLE";

    @Column(name = "position_x", precision = 8, scale = 2)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 8, scale = 2)
    private BigDecimal positionY;

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