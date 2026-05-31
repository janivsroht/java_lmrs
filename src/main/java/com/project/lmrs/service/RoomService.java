package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateRoomRequest;
import com.project.lmrs.dto.response.AvailabilityResponse;
import com.project.lmrs.dto.response.RoomResponse;
import com.project.lmrs.entity.Reservation;
import com.project.lmrs.entity.Room;
import com.project.lmrs.entity.RoomType;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.enums.RoomStatus;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.ReservationRepository;
import com.project.lmrs.repository.RoomRepository;
import com.project.lmrs.repository.RoomTypeRepository;
import com.project.lmrs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final TenantRepository tenantRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<RoomType> getAllRoomTypes(String tenantId) {
        return roomTypeRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms(String tenantId) {
        return roomRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(String roomId, String tenantId) {
        Room room = roomRepository.findByRoomIdAndTenant_TenantIdAndIsDeletedFalse(roomId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailableRooms(String tenantId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> allRooms = roomRepository.findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(tenantId, RoomStatus.AVAILABLE);

        return allRooms.stream()
                .filter(room -> {
                    List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                            room.getRoomId(), checkIn, checkOut);
                    return conflicts.isEmpty();
                })
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse createRoom(String tenantId, CreateRoomRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", request.getRoomTypeId()));

        roomRepository.findByRoomNumberAndTenant_TenantIdAndIsDeletedFalse(request.getRoomNumber(), tenantId)
                .ifPresent(r -> { throw new BusinessRuleException("Room number already exists: " + request.getRoomNumber()); });

        Room room = Room.builder()
                .tenant(tenant)
                .roomNumber(request.getRoomNumber())
                .roomType(roomType)
                .floor(request.getFloor())
                .baseRate(request.getBaseRate())
                .isDeleted(false)
                .build();

        room = roomRepository.save(room);
        return toResponse(room);
    }

    @Transactional
    public RoomResponse updateRoom(String roomId, CreateRoomRequest request, String tenantId) {
        Room room = roomRepository.findByRoomIdAndTenant_TenantIdAndIsDeletedFalse(roomId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", request.getRoomTypeId()));

        roomRepository.findByRoomNumberAndTenant_TenantIdAndIsDeletedFalse(request.getRoomNumber(), room.getTenant().getTenantId())
                .filter(r -> !r.getRoomId().equals(roomId))
                .ifPresent(r -> { throw new BusinessRuleException("Room number already exists: " + request.getRoomNumber()); });

        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(roomType);
        room.setFloor(request.getFloor());
        room.setBaseRate(request.getBaseRate());

        room = roomRepository.save(room);
        return toResponse(room);
    }

    @Transactional
    public void deleteRoom(String roomId, String tenantId) {
        Room room = roomRepository.findByRoomIdAndTenant_TenantIdAndIsDeletedFalse(roomId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        room.setDeleted(true);
        room.setDeletedAt(java.time.LocalDateTime.now());
        roomRepository.save(room);
    }

    private RoomResponse toResponse(Room r) {
        return RoomResponse.builder()
                .roomId(r.getRoomId())
                .roomNumber(r.getRoomNumber())
                .roomTypeId(r.getRoomType().getRoomTypeId())
                .roomTypeName(r.getRoomType().getName())
                .floor(r.getFloor())
                .status(r.getStatus().name())
                .housekeepingStatus(r.getHousekeepingStatus().name())
                .baseRate(r.getBaseRate())
                .build();
    }

    private AvailabilityResponse toAvailabilityResponse(Room r) {
        return AvailabilityResponse.builder()
                .roomId(r.getRoomId())
                .roomNumber(r.getRoomNumber())
                .roomTypeName(r.getRoomType().getName())
                .floor(r.getFloor())
                .baseRate(r.getBaseRate())
                .amenities(r.getRoomType().getAmenities())
                .build();
    }
}
