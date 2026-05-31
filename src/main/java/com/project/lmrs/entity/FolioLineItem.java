package com.project.lmrs.entity;

import com.project.lmrs.enums.ChargeType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "folio_line_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FolioLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_item_id", length = 36)
    private String lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_id", nullable = false)
    private Folio folio;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type_enum", nullable = false, length = 30)
    private ChargeType chargeType;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "posted_by_user_id", length = 36)
    private String postedByUserId;

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
        if (postedAt == null) postedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}