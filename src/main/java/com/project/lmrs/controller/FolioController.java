package com.project.lmrs.controller;

import com.project.lmrs.dto.request.PostChargeRequest;
import com.project.lmrs.dto.response.FolioResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.FolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/folios")
@RequiredArgsConstructor
public class FolioController {

    private final FolioService folioService;

    @GetMapping("/{folioId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK','FINANCE')")
    public ResponseEntity<FolioResponse> getFolioById(@PathVariable String folioId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(folioService.getFolioById(folioId, tenantId));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK','FINANCE')")
    public ResponseEntity<FolioResponse> getFolioByReservation(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(folioService.getFolioByReservation(reservationId, tenantId));
    }

    @PostMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<FolioResponse> createFolio(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(folioService.createFolio(reservationId, tenantId));
    }

    @PostMapping("/{folioId}/charges")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK','SERVER')")
    public ResponseEntity<FolioResponse> postCharge(@PathVariable String folioId,
                                                     @Valid @RequestBody PostChargeRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(folioService.postCharge(folioId, request, tenantId));
    }

    @PutMapping("/{folioId}/close")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FINANCE')")
    public ResponseEntity<Void> closeFolio(@PathVariable String folioId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        folioService.closeFolio(folioId, tenantId);
        return ResponseEntity.noContent().build();
    }
}
