package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateRoomRequest;
import com.project.lmrs.dto.response.AvailabilityResponse;
import com.project.lmrs.dto.response.RoomResponse;
import com.project.lmrs.dto.response.RoomTypeResponse;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.RoomService;
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
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<RoomTypeResponse>> getAllRoomTypes() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<RoomTypeResponse> types = roomService.getAllRoomTypes(tenantId).stream()
                .map(rt -> RoomTypeResponse.builder()
                        .roomTypeId(rt.getRoomTypeId())
                        .name(rt.getName())
                        .maxOccupancy(rt.getMaxOccupancy())
                        .description(rt.getDescription())
                        .amenities(rt.getAmenities())
                        .build())
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(roomService.getAllRooms(tenantId));
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable String roomId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(roomService.getRoomById(roomId, tenantId));
    }

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
    public ResponseEntity<List<AvailabilityResponse>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(roomService.getAvailableRooms(tenantId, checkIn, checkOut));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(tenantId, request));
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable String roomId,
                                                   @Valid @RequestBody CreateRoomRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(roomService.updateRoom(roomId, request, tenantId));
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        roomService.deleteRoom(roomId, tenantId);
        return ResponseEntity.noContent().build();
    }
}
