package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateReservationRequest;
import com.project.lmrs.dto.response.ReservationResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        if (status == null && guestName == null && roomNumber == null && dateFrom == null && dateTo == null) {
            return ResponseEntity.ok(reservationService.getAllReservations(tenantId));
        }
        return ResponseEntity.ok(reservationService.searchReservations(tenantId, status, guestName, roomNumber, dateFrom, dateTo));
    }

    @GetMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(reservationService.getReservationById(reservationId, tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(tenantId, request));
    }

    @PutMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable String reservationId,
                                                                 @Valid @RequestBody CreateReservationRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(reservationService.updateReservation(reservationId, request, tenantId));
    }

    @PutMapping("/{reservationId}/no-show")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<Void> markNoShow(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        reservationService.markNoShow(reservationId, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reservationId}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<Void> cancelReservation(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        reservationService.cancelReservation(reservationId, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reservationId}/check-in")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(reservationService.checkIn(reservationId, tenantId));
    }

    @PutMapping("/{reservationId}/check-out")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<ReservationResponse> checkOut(@PathVariable String reservationId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(reservationService.checkOut(reservationId, tenantId));
    }
}
