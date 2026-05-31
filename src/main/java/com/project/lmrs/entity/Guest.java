package com.project.lmrs.entity;

import com.project.lmrs.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guest_id", length = 36)
    private String guestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    private LocalDate dob;

    @Column(length = 100)
    private String nationality;

    @Column(name = "id_doc_type", length = 50)
    private String idDocType;

    @Column(name = "id_doc_number", length = 100)
    private String idDocNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_tier", length = 20)
    private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;

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