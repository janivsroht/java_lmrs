package com.project.lmrs.controller;

import com.project.lmrs.dto.request.PartnerReservationRequest;
import com.project.lmrs.dto.request.UpdatePartnerReservationRequest;
import com.project.lmrs.dto.response.PartnerReservationResponse;
import com.project.lmrs.dto.response.PartnerRoomListingResponse;
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
@RequestMapping("/api/v1/partner/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
public class PartnerRoomController {

    private final PartnerService partnerService;

    @GetMapping
    public ResponseEntity<List<PartnerRoomListingResponse>> listRooms(
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(partnerService.listAvailableRooms(tenantId));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<PartnerReservationResponse>> listReservations(
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(partnerService.listPartnerReservations(tenantId));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<PartnerReservationResponse> getReservation(
            @PathVariable String reservationId,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(partnerService.getPartnerReservation(reservationId, tenantId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<PartnerReservationResponse> createReservation(
            @Valid @RequestBody PartnerReservationRequest body,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        PartnerAccount partner = (PartnerAccount) request.getAttribute("partner");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(partnerService.createReservation(tenantId, body, partner));
    }

    @PutMapping("/reservations/{reservationId}")
    public ResponseEntity<PartnerReservationResponse> updateReservation(
            @PathVariable String reservationId,
            @Valid @RequestBody UpdatePartnerReservationRequest body,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(
            partnerService.updateReservation(reservationId, body, tenantId));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<PartnerReservationResponse> cancelReservation(
            @PathVariable String reservationId,
            HttpServletRequest request) {
        String tenantId = (String) request.getAttribute("tenantId");
        return ResponseEntity.ok(
            partnerService.cancelReservation(reservationId, tenantId));
    }
}
