package com.project.lmrs.controller;

import com.project.lmrs.dto.response.UsageDashboardResponse;
import com.project.lmrs.entity.PartnerAccount;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/partners")
@RequiredArgsConstructor
public class PartnerManagementController {

    private final PartnerService partnerService;
    private static final String PARTNER_TEMPLATE = "dashboard/partners";

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public String partnersPage() {
        return PARTNER_TEMPLATE;
    }

    @GetMapping("/{partnerId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public String partnerDashboardPage() {
        return "dashboard/partner-dashboard";
    }

    @RestController
    @RequestMapping("/api/v1/admin/partners")
    @RequiredArgsConstructor
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public static class PartnerAdminRestController {

        private final PartnerService partnerService;

        @GetMapping
        public ResponseEntity<List<PartnerAccount>> listPartners() {
            String tenantId = SecurityUtils.getCurrentTenantId();
            return ResponseEntity.ok(partnerService.listPartners(tenantId));
        }

        @PostMapping
        public ResponseEntity<PartnerAccount> createPartner(@RequestBody Map<String, String> body) {
            String tenantId = SecurityUtils.getCurrentTenantId();
            PartnerAccount partner = partnerService.createPartner(
                    body.get("name"), body.get("providerType"), tenantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(partner);
        }

        @GetMapping("/{partnerId}")
        public ResponseEntity<PartnerAccount> getPartner(@PathVariable String partnerId) {
            String tenantId = SecurityUtils.getCurrentTenantId();
            return ResponseEntity.ok(partnerService.getPartnerAccount(partnerId, tenantId));
        }

        @PutMapping("/{partnerId}/status")
        public ResponseEntity<Void> togglePartnerStatus(
                @PathVariable String partnerId, @RequestBody Map<String, Boolean> body) {
            String tenantId = SecurityUtils.getCurrentTenantId();
            partnerService.togglePartnerStatus(partnerId, body.get("isActive"), tenantId);
            return ResponseEntity.ok().build();
        }

        @GetMapping("/{partnerId}/usage")
        public ResponseEntity<UsageDashboardResponse> getUsageDashboard(@PathVariable String partnerId) {
            String tenantId = SecurityUtils.getCurrentTenantId();
            return ResponseEntity.ok(partnerService.getUsageDashboard(partnerId, tenantId));
        }
    }
}
