package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateTableReservationRequest;
import com.project.lmrs.dto.response.TableReservationResponse;
import com.project.lmrs.entity.TableReservation;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.TableReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/table-reservations")
@RequiredArgsConstructor
public class TableReservationController {

    private final TableReservationService tableReservationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<List<TableReservationResponse>> getAllTableReservations() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(tableReservationService.getAllTableReservations(tenantId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/{tableResId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<TableReservationResponse> getTableReservationById(@PathVariable String tableResId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(tableReservationService.getTableReservationById(tableResId, tenantId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<TableReservationResponse> createTableReservation(
            @Valid @RequestBody CreateTableReservationRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(tableReservationService.createTableReservation(tenantId, request)));
    }

    @PutMapping("/{tableResId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<TableReservationResponse> updateTableReservation(
            @PathVariable String tableResId,
            @Valid @RequestBody CreateTableReservationRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(toResponse(tableReservationService.updateTableReservation(tableResId, request, tenantId)));
    }

    @DeleteMapping("/{tableResId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','SERVER','FRONT_DESK')")
    public ResponseEntity<Void> cancelTableReservation(@PathVariable String tableResId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        tableReservationService.cancelTableReservation(tableResId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private TableReservationResponse toResponse(TableReservation tr) {
        return TableReservationResponse.builder()
                .tableResId(tr.getTableResId())
                .tableId(tr.getTable().getTableId())
                .tableNumber(tr.getTable().getTableNumber())
                .guestId(tr.getGuest() != null ? tr.getGuest().getGuestId() : null)
                .guestName(tr.getGuest() != null
                        ? tr.getGuest().getFirstName() + " " + tr.getGuest().getLastName() : null)
                .partySize(tr.getPartySize())
                .reservationDt(tr.getReservationDt())
                .status(tr.getStatus())
                .specialNotes(tr.getSpecialNotes())
                .build();
    }
}
