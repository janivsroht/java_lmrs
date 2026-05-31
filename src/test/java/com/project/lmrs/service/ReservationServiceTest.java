package com.project.lmrs.service;

import com.project.lmrs.dto.request.CreateReservationRequest;
import com.project.lmrs.dto.response.ReservationResponse;
import com.project.lmrs.entity.*;
import com.project.lmrs.enums.BookingChannel;
import com.project.lmrs.enums.ReservationStatus;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private FolioRepository folioRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, guestRepository, roomRepository,
                tenantRepository, folioRepository);
    }

    @Test
    void getAllReservations_shouldReturnList() {
        String tenantId = "t1";
        RoomType roomType = new RoomType();
        roomType.setName("Deluxe");

        Guest guest = new Guest();
        guest.setGuestId("g1");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setRoomId("r1");
        room.setRoomNumber("101");
        room.setRoomType(roomType);

        Reservation res = Reservation.builder()
                .reservationId("res1")
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .rateApplied(BigDecimal.valueOf(200))
                .status(ReservationStatus.CONFIRMED)
                .channel(BookingChannel.DIRECT)
                .build();

        when(reservationRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of(res));

        List<ReservationResponse> results = reservationService.getAllReservations(tenantId);
        assertEquals(1, results.size());
        assertEquals("res1", results.get(0).getReservationId());
    }

    @Test
    void getReservationById_notFound_shouldThrow() {
        when(reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse("badId", "t1"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationById("badId", "t1"));
    }

    @Test
    void createReservation_roomConflict_shouldThrow() {
        String tenantId = "t1";
        CreateReservationRequest request = new CreateReservationRequest();
        request.setGuestId("g1");
        request.setRoomId("r1");
        request.setCheckInDate(LocalDate.now());
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(new Tenant()));
        when(guestRepository.findById("g1")).thenReturn(Optional.of(new Guest()));
        when(roomRepository.findById("r1")).thenReturn(Optional.of(new Room()));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(List.of(new Reservation()));

        assertThrows(BusinessRuleException.class,
                () -> reservationService.createReservation(tenantId, request));
    }

    @Test
    void cancelReservation_shouldSucceed() {
        String tenantId = "t1";
        Room room = new Room();
        room.setRoomId("r1");

        Reservation reservation = Reservation.builder()
                .reservationId("res1")
                .room(room)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findByReservationIdAndTenant_TenantIdAndIsDeletedFalse("res1", tenantId))
                .thenReturn(Optional.of(reservation));

        reservationService.cancelReservation("res1", tenantId);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void searchReservations_shouldDelegateToRepository() {
        String tenantId = "t1";
        when(reservationRepository.searchReservations(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<ReservationResponse> results = reservationService.searchReservations(tenantId, "CONFIRMED",
                "John", "101", LocalDate.now(), LocalDate.now().plusDays(7));

        assertTrue(results.isEmpty());
        verify(reservationRepository).searchReservations(tenantId, ReservationStatus.CONFIRMED,
                "John", "101", LocalDate.now(), LocalDate.now().plusDays(7));
    }
}
