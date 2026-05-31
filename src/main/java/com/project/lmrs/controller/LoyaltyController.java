package com.project.lmrs.controller;

import com.project.lmrs.dto.request.RedeemPointsRequest;
import com.project.lmrs.dto.response.LoyaltyTransactionResponse;
import com.project.lmrs.entity.LoyaltyTransaction;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/guest/{guestId}/balance")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<Map<String, Object>> getPointsBalance(@PathVariable String guestId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        Integer balance = loyaltyService.getPointsBalance(guestId, tenantId);
        return ResponseEntity.ok(Map.of("guestId", guestId, "balance", balance));
    }

    @GetMapping("/guest/{guestId}/history")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getTransactionHistory(@PathVariable String guestId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(loyaltyService.getTransactionHistory(guestId, tenantId).stream().map(this::toResponse).toList());
    }

    @PostMapping("/guest/{guestId}/redeem")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<LoyaltyTransactionResponse> redeemPoints(@PathVariable String guestId,
                                                                     @Valid @RequestBody RedeemPointsRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        LoyaltyTransaction transaction = loyaltyService.redeemPoints(
                guestId,
                request.getPoints(),
                request.getReferenceId(),
                request.getReferenceType(),
                tenantId
        );
        return ResponseEntity.ok(toResponse(transaction));
    }

    private LoyaltyTransactionResponse toResponse(LoyaltyTransaction t) {
        return LoyaltyTransactionResponse.builder()
                .loyaltyTxId(t.getLoyaltyTxId())
                .transactionType(t.getTransactionType())
                .points(t.getPoints())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
