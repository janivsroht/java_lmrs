package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateReservationRequest;
import com.project.lmrs.dto.response.ReservationResponse;
import com.project.lmrs.entity.*;
import com.project.lmrs.enums.BookingChannel;
import com.project.lmrs.enums.HousekeepingStatus;
import com.project.lmrs.enums.ReservationStatus;
import com.project.lmrs.enums.RoomStatus;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final FolioRepository folioRepository;
    private final Set<String> folioCreationLocks = ConcurrentHashMap.newKeySet();

    public List<ReservationResponse> getAllReservations(String tenantId) {
        return reservationRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse getReservationById(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse createReservation(String tenantId, CreateReservationRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Guest guest = guestRepository.findById(request.getGuestId())
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", request.getGuestId()));

        Room room = roomRepository.findById(request.getRoomId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                room.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("Room is not available for the selected dates");
        }

        BookingChannel channel = request.getChannel() != null
                ? BookingChannel.valueOf(request.getChannel())
                : BookingChannel.DIRECT;

        Reservation reservation = Reservation.builder()
                .tenant(tenant)
                .guest(guest)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .rateApplied(request.getRateApplied())
                .channel(channel)
                .specialRequests(request.getSpecialRequests())
                .isDeleted(false)
                .build();

        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(String reservationId, CreateReservationRequest request, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN ||
            reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BusinessRuleException("Cannot modify a " + reservation.getStatus() + " reservation");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                room.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
        boolean conflictWithSelf = conflicts.stream()
                .anyMatch(r -> r.getReservationId().equals(reservationId));
        if (conflicts.size() > 1 || (!conflictWithSelf && !conflicts.isEmpty())) {
            throw new BusinessRuleException("Room is not available for the selected dates");
        }

        Guest guest = guestRepository.findById(request.getGuestId())
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", request.getGuestId()));

        BookingChannel channel = request.getChannel() != null
                ? BookingChannel.valueOf(request.getChannel())
                : reservation.getChannel();

        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setRateApplied(request.getRateApplied());
        reservation.setChannel(channel);
        reservation.setSpecialRequests(request.getSpecialRequests());

        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public void markNoShow(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
            reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING or CONFIRMED reservations can be marked as no-show");
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);
        reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BusinessRuleException("Cannot cancel a checked-out reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse checkIn(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only CONFIRMED reservations can be checked in");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.getRoom().setStatus(RoomStatus.OCCUPIED);

        Reservation saved = reservationRepository.save(reservation);

        if (folioCreationLocks.add(saved.getReservationId())) {
            try {
                if (folioRepository.findByReservation_ReservationIdAndIsDeletedFalse(saved.getReservationId()).isEmpty()) {
                    Folio folio = Folio.builder()
                            .reservation(saved)
                            .guest(saved.getGuest())
                            .currency("INR")
                            .totalAmount(BigDecimal.ZERO)
                            .status("OPEN")
                            .isDeleted(false)
                            .build();
                    folioRepository.save(folio);
                }
            } finally {
                folioCreationLocks.remove(saved.getReservationId());
            }
        }

        return toResponse(saved);
    }

    @Transactional
    public ReservationResponse checkOut(String reservationId, String tenantId) {
        Reservation reservation = reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Only CHECKED_IN reservations can be checked out");
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        reservation.getRoom().setHousekeepingStatus(HousekeepingStatus.DIRTY);

        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    public List<ReservationResponse> searchReservations(String tenantId, String status, String guestName,
                                                         String roomNumber, LocalDate dateFrom, LocalDate dateTo) {
        ReservationStatus statusEnum = status != null && !status.isEmpty() ? ReservationStatus.valueOf(status) : null;
        String name = guestName != null && !guestName.isBlank() ? guestName.trim() : null;
        String room = roomNumber != null && !roomNumber.isBlank() ? roomNumber.trim() : null;
        return reservationRepository.searchReservations(tenantId, statusEnum, name, room, dateFrom, dateTo)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .reservationId(r.getReservationId())
                .guestId(r.getGuest().getGuestId())
                .guestName(r.getGuest().getFirstName() + " " + r.getGuest().getLastName())
                .roomId(r.getRoom().getRoomId())
                .roomNumber(r.getRoom().getRoomNumber())
                .roomTypeName(r.getRoom().getRoomType().getName())
                .checkInDate(r.getCheckInDate())
                .checkOutDate(r.getCheckOutDate())
                .status(r.getStatus().name())
                .channel(r.getChannel().name())
                .rateApplied(r.getRateApplied())
                .specialRequests(r.getSpecialRequests())
                .build();
    }
}
