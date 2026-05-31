package com.project.lmrs.controller;

import com.project.lmrs.dto.request.PartnerTableReservationRequest;
import com.project.lmrs.dto.response.PartnerMenuListingResponse;
import com.project.lmrs.dto.response.PartnerTableReservationResponse;
import com.project.lmrs.entity.PartnerAccount;
import com.project.lmrs.service.PartnerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partner/restaurant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
public class PartnerRestaurantController {

    private final PartnerService partnerService;

    @GetMapping("/menu")
    public ResponseEntity<List<PartnerMenuListingResponse>> listMenu(
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(partnerService.listMenuItems(tenantId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<PartnerTableReservationResponse> createTableReservation(
            @Valid @RequestBody PartnerTableReservationRequest body,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        PartnerAccount partner = (PartnerAccount) request.getAttribute("partner");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(partnerService.createTableReservation(tenantId, body, partner));
    }

    @PutMapping("/reservations/{tableResId}")
    public ResponseEntity<PartnerTableReservationResponse> updateTableReservation(
            @PathVariable String tableResId,
            @Valid @RequestBody PartnerTableReservationRequest body,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(
            partnerService.updateTableReservation(tableResId, body, tenantId));
    }

    @DeleteMapping("/reservations/{tableResId}")
    public ResponseEntity<PartnerTableReservationResponse> cancelTableReservation(
            @PathVariable String tableResId,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(
            partnerService.cancelTableReservation(tableResId, tenantId));
    }
}
