package com.project.lmrs.controller;

import com.project.lmrs.dto.response.DashboardSummaryResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(dashboardService.getSummary(tenantId));
    }
}
