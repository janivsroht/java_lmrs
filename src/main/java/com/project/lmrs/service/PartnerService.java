package com.project.lmrs.service;

import com.project.lmrs.dto.request.*;
import com.project.lmrs.dto.response.*;
import com.project.lmrs.entity.*;
import com.project.lmrs.enums.BookingChannel;
import com.project.lmrs.enums.ReservationStatus;
import com.project.lmrs.enums.RoomStatus;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemVariantRepository menuItemVariantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final TableReservationRepository tableReservationRepository;
    private final PartnerApiUsageRepository partnerApiUsageRepository;
    private final PartnerAccountRepository partnerAccountRepository;

    // ── LISTING ──────────────────────────────────────────────────────

    public List<PartnerRoomListingResponse> listAvailableRooms(String tenantId) {
        return roomRepository
            .findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, RoomStatus.AVAILABLE)
            .stream()
            .map(room -> PartnerRoomListingResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomTypeName(room.getRoomType().getName())
                .floor(room.getFloor() != null ? room.getFloor() : 0)
                .maxOccupancy(room.getRoomType().getMaxOccupancy())
                .status(room.getStatus().name())
                .baseRate(room.getBaseRate())
                .amenities(room.getRoomType().getAmenities())
                .build())
            .collect(Collectors.toList());
    }

    public List<PartnerMenuListingResponse> listMenuItems(String tenantId) {
        return menuItemRepository
            .findAllByTenant_TenantIdAndIsAvailableTrueAndIsDeletedFalse(tenantId)
            .stream()
            .map(item -> {
                List<PartnerMenuListingResponse.VariantDto> variants =
                    menuItemVariantRepository
                        .findAllByMenuItem_ItemIdAndIsDeletedFalse(item.getItemId())
                        .stream()
                        .map(v -> PartnerMenuListingResponse.VariantDto.builder()
                            .variantId(v.getVariantId())
                            .name(v.getName())
                            .priceModifier(v.getPriceModifier())
                            .build())
                        .collect(Collectors.toList());

                return PartnerMenuListingResponse.builder()
                    .itemId(item.getItemId())
                    .categoryName(item.getCategory().getName())
                    .name(item.getName())
                    .description(item.getDescription())
                    .basePrice(item.getBasePrice())
                    .allergens(item.getAllergens())
                    .dietaryFlags(item.getDietaryFlags())
                    .isAvailable(item.isAvailable())
                    .variants(variants)
                    .build();
            })
            .collect(Collectors.toList());
    }

    // ── ROOM RESERVATIONS ─────────────────────────────────────────────

    @Transactional
    public PartnerReservationResponse createReservation(
            String tenantId, PartnerReservationRequest req, PartnerAccount partner) {

        Guest guest = guestRepository
            .findByEmailAndTenant_TenantIdAndIsDeletedFalse(req.getGuestEmail(), tenantId)
            .orElseGet(() -> guestRepository.save(Guest.builder()
                .tenant(partner.getTenant())
                .firstName(req.getGuestFirstName())
                .lastName(req.getGuestLastName())
                .email(req.getGuestEmail())
                .phone(req.getGuestPhone())
                .build()));

        Room room = roomRepository
            .findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, RoomStatus.AVAILABLE)
            .stream()
            .filter(r -> r.getRoomType().getRoomTypeId().equals(req.getRoomTypeId()))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("No available room of requested type"));

        List<Reservation> conflicts = reservationRepository
            .findConflictingReservations(room.getRoomId(), req.getCheckInDate(), req.getCheckOutDate());
        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("Room not available for selected dates");
        }

        Reservation reservation = reservationRepository.save(Reservation.builder()
            .tenant(partner.getTenant())
            .guest(guest)
            .room(room)
            .checkInDate(req.getCheckInDate())
            .checkOutDate(req.getCheckOutDate())
            .status(ReservationStatus.CONFIRMED)
            .channel(BookingChannel.DIRECT)
            .rateApplied(room.getBaseRate())
            .specialRequests(req.getSpecialRequests())
            .build());

        return toReservationResponse(reservation);
    }

    @Transactional
    public PartnerReservationResponse updateReservation(
            String reservationId, UpdatePartnerReservationRequest req, String tenantId) {

        Reservation res = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        validateTenantOwnership(res.getTenant().getTenantId(), tenantId);

        if (res.getStatus() == ReservationStatus.CANCELLED ||
            res.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BusinessRuleException("Cannot update a cancelled or completed reservation");
        }

        if (req.getCheckInDate() != null)    res.setCheckInDate(req.getCheckInDate());
        if (req.getCheckOutDate() != null)   res.setCheckOutDate(req.getCheckOutDate());
        if (req.getSpecialRequests() != null) res.setSpecialRequests(req.getSpecialRequests());

        return toReservationResponse(reservationRepository.save(res));
    }

    @Transactional
    public PartnerReservationResponse cancelReservation(String reservationId, String tenantId) {
        Reservation res = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        validateTenantOwnership(res.getTenant().getTenantId(), tenantId);

        if (res.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Cannot cancel a reservation that is already checked in");
        }

        res.setStatus(ReservationStatus.CANCELLED);
        return toReservationResponse(reservationRepository.save(res));
    }

    // ── TABLE RESERVATIONS ────────────────────────────────────────────

    @Transactional
    public PartnerTableReservationResponse createTableReservation(
            String tenantId, PartnerTableReservationRequest req, PartnerAccount partner) {

        Guest guest = guestRepository
            .findByEmailAndTenant_TenantIdAndIsDeletedFalse(req.getGuestEmail(), tenantId)
            .orElseGet(() -> guestRepository.save(Guest.builder()
                .tenant(partner.getTenant())
                .firstName(req.getGuestFirstName())
                .lastName(req.getGuestLastName())
                .email(req.getGuestEmail())
                .phone(req.getGuestPhone())
                .build()));

        RestaurantTable table = restaurantTableRepository
            .findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, "AVAILABLE")
            .stream()
            .filter(t -> t.getCapacity() >= req.getPartySize())
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException("No available table for party size"));

        TableReservation tableRes = tableReservationRepository.save(TableReservation.builder()
            .tenant(partner.getTenant())
            .guest(guest)
            .table(table)
            .partySize(req.getPartySize())
            .reservationDt(req.getReservationDateTime())
            .status("CONFIRMED")
            .specialNotes(req.getSpecialNotes())
            .build());

        return toTableReservationResponse(tableRes);
    }

    @Transactional
    public PartnerTableReservationResponse updateTableReservation(
            String tableResId, PartnerTableReservationRequest req, String tenantId) {

        TableReservation tr = tableReservationRepository.findById(tableResId)
            .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", tableResId));

        validateTenantOwnership(tr.getTenant().getTenantId(), tenantId);

        if (req.getReservationDateTime() != null) tr.setReservationDt(req.getReservationDateTime());
        if (req.getSpecialNotes() != null)         tr.setSpecialNotes(req.getSpecialNotes());

        return toTableReservationResponse(tableReservationRepository.save(tr));
    }

    @Transactional
    public PartnerTableReservationResponse cancelTableReservation(
            String tableResId, String tenantId) {

        TableReservation tr = tableReservationRepository.findById(tableResId)
            .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", tableResId));

        validateTenantOwnership(tr.getTenant().getTenantId(), tenantId);
        tr.setStatus("CANCELLED");
        return toTableReservationResponse(tableReservationRepository.save(tr));
    }

    // ── DASHBOARD ─────────────────────────────────────────────────────

    public UsageDashboardResponse getUsageDashboard(String partnerId, String tenantId) {
        PartnerAccount partner = partnerAccountRepository.findById(partnerId)
            .orElseThrow(() -> new ResourceNotFoundException("PartnerAccount", "id", partnerId));

        validateTenantOwnership(partner.getTenant().getTenantId(), tenantId);

        List<PartnerApiUsage> usages = partnerApiUsageRepository
            .findAllByPartner_PartnerId(partnerId);

        long total   = usages.size();
        long success = usages.stream().filter(u -> u.getStatusCode() < 400).count();
        long errors  = total - success;
        double avgMs = usages.stream().mapToInt(PartnerApiUsage::getResponseMs).average().orElse(0);

        List<UsageDashboardResponse.EndpointStat> stats =
            partnerApiUsageRepository.getEndpointStats(partnerId)
                .stream()
                .map(row -> UsageDashboardResponse.EndpointStat.builder()
                    .endpoint((String) row[0])
                    .callCount((Long) row[1])
                    .avgResponseMs((Double) row[2])
                    .build())
                .collect(Collectors.toList());

        return UsageDashboardResponse.builder()
            .partnerId(partnerId)
            .partnerName(partner.getName())
            .providerType(partner.getProviderType())
            .totalCalls(total)
            .avgResponseMs(avgMs)
            .successCalls(success)
            .errorCalls(errors)
            .endpointStats(stats)
            .build();
    }

    // ── PARTNER ACCOUNT CREATION ──────────────────────────────────────

    @Transactional
    public PartnerAccount createPartner(String name, String providerType, Tenant tenant) {
        String apiKey = "lrms_" + UUID.randomUUID().toString().replace("-", "");
        return partnerAccountRepository.save(PartnerAccount.builder()
            .tenant(tenant)
            .name(name)
            .providerType(providerType.toUpperCase())
            .apiKey(apiKey)
            .isActive(true)
            .build());
    }

    // ── HELPERS ───────────────────────────────────────────────────────

    private void validateTenantOwnership(String resourceTenantId, String requestTenantId) {
        if (!resourceTenantId.equals(requestTenantId)) {
            throw new BusinessRuleException("Access denied: resource belongs to different tenant");
        }
    }

    private PartnerReservationResponse toReservationResponse(Reservation r) {
        return PartnerReservationResponse.builder()
            .reservationId(r.getReservationId())
            .guestName(r.getGuest().getFirstName() + " " + r.getGuest().getLastName())
            .guestEmail(r.getGuest().getEmail())
            .roomNumber(r.getRoom().getRoomNumber())
            .roomType(r.getRoom().getRoomType().getName())
            .checkInDate(r.getCheckInDate())
            .checkOutDate(r.getCheckOutDate())
            .status(r.getStatus().name())
            .rateApplied(r.getRateApplied())
            .specialRequests(r.getSpecialRequests())
            .bookingChannel(r.getChannel().name())
            .build();
    }

    private PartnerTableReservationResponse toTableReservationResponse(TableReservation tr) {
        return PartnerTableReservationResponse.builder()
            .tableReservationId(tr.getTableResId())
            .guestName(tr.getGuest() != null
                ? tr.getGuest().getFirstName() + " " + tr.getGuest().getLastName() : "Walk-in")
            .guestEmail(tr.getGuest() != null ? tr.getGuest().getEmail() : null)
            .tableNumber(tr.getTable().getTableNumber())
            .partySize(tr.getPartySize())
            .reservationDateTime(tr.getReservationDt())
            .status(tr.getStatus())
            .specialNotes(tr.getSpecialNotes())
            .build();
    }
}
