package com.project.lmrs.repository;

import com.project.lmrs.entity.Reservation;
import com.project.lmrs.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<Reservation> findAllByGuest_GuestIdAndIsDeletedFalse(String guestId);
    List<Reservation> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, ReservationStatus status);
    Optional<Reservation> findByReservationIdAndTenant_TenantIdAndIsDeletedFalse(String reservationId, String tenantId);

    @Query("SELECT r FROM Reservation r WHERE r.room.roomId = :roomId " +
            "AND r.isDeleted = false " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
            "AND r.checkInDate < :checkOut AND r.checkOutDate > :checkIn")
    List<Reservation> findConflictingReservations(
            @Param("roomId") String roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT r.status, COUNT(r) FROM Reservation r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "GROUP BY r.status")
    List<Object[]> countByStatusGrouped(@Param("tenantId") String tenantId);

    @Query("SELECT r.channel, COUNT(r) FROM Reservation r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "GROUP BY r.channel")
    List<Object[]> countByChannelGrouped(@Param("tenantId") String tenantId);

    @Query("SELECT r.checkInDate, COUNT(r) FROM Reservation r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "AND r.createdAt BETWEEN :from AND :to " +
            "GROUP BY r.checkInDate ORDER BY r.checkInDate")
    List<Object[]> countByDateRange(@Param("tenantId") String tenantId,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    long countByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    long countByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, ReservationStatus status);

    Page<Reservation> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);
    Page<Reservation> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, ReservationStatus status, Pageable pageable);
    Page<Reservation> findAllByGuest_GuestIdAndIsDeletedFalse(String guestId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.rateApplied), 0) FROM Reservation r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "AND r.status IN ('CHECKED_IN', 'CHECKED_OUT', 'CONFIRMED')")
    BigDecimal sumRevenueByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT r FROM Reservation r WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:guestName IS NULL OR CONCAT(r.guest.firstName, ' ', r.guest.lastName) LIKE %:guestName%) " +
            "AND (:roomNumber IS NULL OR r.room.roomNumber LIKE %:roomNumber%) " +
            "AND (:dateFrom IS NULL OR r.checkInDate >= :dateFrom) " +
            "AND (:dateTo IS NULL OR r.checkInDate <= :dateTo) " +
            "ORDER BY r.checkInDate DESC")
    List<Reservation> searchReservations(@Param("tenantId") String tenantId,
                                         @Param("status") ReservationStatus status,
                                         @Param("guestName") String guestName,
                                         @Param("roomNumber") String roomNumber,
                                         @Param("dateFrom") LocalDate dateFrom,
                                         @Param("dateTo") LocalDate dateTo);

    @Query("SELECT r.checkInDate, COALESCE(SUM(r.rateApplied), 0) FROM Reservation r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "AND r.checkInDate BETWEEN :from AND :to " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "GROUP BY r.checkInDate ORDER BY r.checkInDate")
    List<Object[]> revenueByDateRange(@Param("tenantId") String tenantId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);
}