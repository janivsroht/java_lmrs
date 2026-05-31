package com.project.lmrs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_api_usage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartnerApiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "usage_id", length = 36)
    private String usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private PartnerAccount partner;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "response_ms", nullable = false)
    private int responseMs;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
