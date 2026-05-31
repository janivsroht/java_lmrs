package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateGuestRequest;
import com.project.lmrs.dto.response.GuestResponse;
import com.project.lmrs.entity.Guest;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.GuestRepository;
import com.project.lmrs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final TenantRepository tenantRepository;

    public List<GuestResponse> getAllGuests(String tenantId) {
        return guestRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<GuestResponse> getAllGuestsPaged(String tenantId, Pageable pageable) {
        return guestRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId, pageable)
                .map(this::toResponse);
    }

    public GuestResponse getGuestById(String guestId, String tenantId) {
        Guest guest = guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));
        return toResponse(guest);
    }

    public List<GuestResponse> searchByLastName(String tenantId, String lastName) {
        return guestRepository
                .findAllByTenant_TenantIdAndLastNameContainingIgnoreCaseAndIsDeletedFalse(tenantId, lastName)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GuestResponse createGuest(String tenantId, CreateGuestRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Guest guest = Guest.builder()
                .tenant(tenant)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dob(request.getDob())
                .nationality(request.getNationality())
                .idDocType(request.getIdDocType())
                .idDocNumber(request.getIdDocNumber())
                .isDeleted(false)
                .build();

        guest = guestRepository.save(guest);
        return toResponse(guest);
    }

    @Transactional
    public GuestResponse updateGuest(String guestId, CreateGuestRequest request, String tenantId) {
        Guest guest = guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));

        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setDob(request.getDob());
        guest.setNationality(request.getNationality());
        guest.setIdDocType(request.getIdDocType());
        guest.setIdDocNumber(request.getIdDocNumber());

        guest = guestRepository.save(guest);
        return toResponse(guest);
    }

    @Transactional
    public void deleteGuest(String guestId, String tenantId) {
        Guest guest = guestRepository.findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(guestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));

        guest.setDeleted(true);
        guest.setDeletedAt(java.time.LocalDateTime.now());
        guestRepository.save(guest);
    }

    private GuestResponse toResponse(Guest g) {
        return GuestResponse.builder()
                .guestId(g.getGuestId())
                .firstName(g.getFirstName())
                .lastName(g.getLastName())
                .email(g.getEmail())
                .phone(g.getPhone())
                .dob(g.getDob())
                .nationality(g.getNationality())
                .idDocType(g.getIdDocType())
                .idDocNumber(g.getIdDocNumber())
                .loyaltyTier(g.getLoyaltyTier() != null ? g.getLoyaltyTier().name() : null)
                .build();
    }
}
