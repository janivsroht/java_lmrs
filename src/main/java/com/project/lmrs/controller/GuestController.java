package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateGuestRequest;
import com.project.lmrs.dto.response.GuestResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<GuestResponse>> getAllGuests() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(guestService.getAllGuests(tenantId));
    }

    @GetMapping("/{guestId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<GuestResponse> getGuestById(@PathVariable String guestId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(guestService.getGuestById(guestId, tenantId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<GuestResponse>> searchByLastName(@RequestParam String lastName) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(guestService.searchByLastName(tenantId, lastName));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<Page<GuestResponse>> getGuestsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(guestService.getAllGuestsPaged(tenantId, pageRequest));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody CreateGuestRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.createGuest(tenantId, request));
    }

    @PutMapping("/{guestId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<GuestResponse> updateGuest(@PathVariable String guestId,
                                                     @Valid @RequestBody CreateGuestRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(guestService.updateGuest(guestId, request, tenantId));
    }

    @DeleteMapping("/{guestId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteGuest(@PathVariable String guestId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        guestService.deleteGuest(guestId, tenantId);
        return ResponseEntity.noContent().build();
    }
}
