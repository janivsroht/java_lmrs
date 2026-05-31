package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id", length = 36)
    private String inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "unit_enum", nullable = false, length = 30)
    private String unit;

    @Column(name = "current_stock", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "reorder_threshold", nullable = false, precision = 10, scale = 3)
    private BigDecimal reorderThreshold = BigDecimal.ZERO;

    @Column(name = "cost_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

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