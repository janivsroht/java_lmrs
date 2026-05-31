package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "loyalty_tx_id", length = 36)
    private String loyaltyTxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(name = "transaction_type_enum", nullable = false, length = 30)
    private String transactionType;

    @Column(nullable = false)
    private int points;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(name = "reference_type_enum", length = 50)
    private String referenceType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}