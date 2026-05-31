package com.project.lmrs.controller;

import com.project.lmrs.dto.response.UsageDashboardResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner/dashboard")
@RequiredArgsConstructor
public class PartnerDashboardController {

    private final PartnerService partnerService;

    @GetMapping("/{partnerId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UsageDashboardResponse> getDashboard(
            @PathVariable String partnerId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(partnerService.getUsageDashboard(partnerId, tenantId));
    }
}
