package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_item_variants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", length = 36)
    private String variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_modifier", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceModifier = BigDecimal.ZERO;

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