package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateTenantRequest;
import com.project.lmrs.dto.response.TenantResponse;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId) {
        return ResponseEntity.ok(toResponse(tenantService.getTenantById(tenantId)));
    }

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(tenantService.createTenant(request.getName(), request.getSubdomain(), request.getConfigJson())));
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> updateTenant(@PathVariable String tenantId,
                                                @RequestBody CreateTenantRequest request) {
        return ResponseEntity.ok(toResponse(tenantService.updateTenant(tenantId, request.getName(), request.getConfigJson())));
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .configJson(tenant.getConfigJson())
                .isActive(tenant.isActive())
                .build();
    }
}
