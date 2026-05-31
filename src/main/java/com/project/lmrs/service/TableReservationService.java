package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateTableReservationRequest;
import com.project.lmrs.entity.*;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableReservationService {

    private final TableReservationRepository tableReservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final GuestRepository guestRepository;
    private final TenantRepository tenantRepository;

    public List<TableReservation> getAllTableReservations(String tenantId) {
        return tableReservationRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId);
    }

    public TableReservation getTableReservationById(String tableResId, String tenantId) {
        return tableReservationRepository.findByTableResIdAndTenant_TenantIdAndIsDeletedFalse(tableResId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", tableResId));
    }

    @Transactional
    public TableReservation createTableReservation(String tenantId, CreateTableReservationRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        RestaurantTable table = restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(request.getTableId(), tenantId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", request.getTableId()));

        Guest guest = null;
        if (request.getGuestId() != null) {
            guest = guestRepository.findById(request.getGuestId())
                    .filter(g -> !g.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", request.getGuestId()));
        }

        List<TableReservation> conflicts = tableReservationRepository
                .findAllByTable_TableIdAndReservationDtBetweenAndIsDeletedFalse(
                        table.getTableId(),
                        request.getReservationDt().minusHours(2),
                        request.getReservationDt().plusHours(2));
        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("Table is not available for the selected time");
        }

        TableReservation reservation = TableReservation.builder()
                .tenant(tenant)
                .guest(guest)
                .table(table)
                .partySize(request.getPartySize())
                .reservationDt(request.getReservationDt())
                .specialNotes(request.getSpecialNotes())
                .isDeleted(false)
                .build();

        return tableReservationRepository.save(reservation);
    }

    @Transactional
    public TableReservation updateTableReservation(String tableResId, CreateTableReservationRequest request, String tenantId) {
        TableReservation reservation = tableReservationRepository.findByTableResIdAndTenant_TenantIdAndIsDeletedFalse(tableResId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", tableResId));

        RestaurantTable table = restaurantTableRepository.findByTableIdAndTenant_TenantIdAndIsDeletedFalse(request.getTableId(), tenantId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", request.getTableId()));

        Guest guest = null;
        if (request.getGuestId() != null) {
            guest = guestRepository.findById(request.getGuestId())
                    .filter(g -> !g.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", request.getGuestId()));
        }

        // Conflict check (exclude self)
        List<TableReservation> conflicts = tableReservationRepository
                .findAllByTable_TableIdAndReservationDtBetweenAndIsDeletedFalse(
                        table.getTableId(),
                        request.getReservationDt().minusHours(2),
                        request.getReservationDt().plusHours(2));
        boolean conflictWithSelf = conflicts.stream()
                .anyMatch(c -> c.getTableResId().equals(tableResId));
        if (conflicts.size() > 1 || (!conflictWithSelf && !conflicts.isEmpty())) {
            throw new BusinessRuleException("Table is not available for the selected time");
        }

        reservation.setTable(table);
        reservation.setGuest(guest);
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationDt(request.getReservationDt());
        reservation.setSpecialNotes(request.getSpecialNotes());

        return tableReservationRepository.save(reservation);
    }

    @Transactional
    public void cancelTableReservation(String tableResId, String tenantId) {
        TableReservation reservation = tableReservationRepository.findByTableResIdAndTenant_TenantIdAndIsDeletedFalse(tableResId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", tableResId));
        reservation.setDeleted(true);
        tableReservationRepository.save(reservation);
    }
}
