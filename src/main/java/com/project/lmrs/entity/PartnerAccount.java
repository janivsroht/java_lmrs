package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartnerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "partner_id", length = 36)
    private String partnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "provider_type", nullable = false, length = 50)
    private String providerType;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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
