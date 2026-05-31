package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoyaltyTransactionResponse {
    private String loyaltyTxId;
    private String transactionType;
    private int points;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
