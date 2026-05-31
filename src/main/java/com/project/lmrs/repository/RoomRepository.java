package com.project.lmrs.repository;

import com.project.lmrs.entity.Room;
import com.project.lmrs.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<Room> findAllByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, RoomStatus status);
    Optional<Room> findByRoomNumberAndTenant_TenantIdAndIsDeletedFalse(String roomNumber, String tenantId);
    Optional<Room> findByRoomIdAndTenant_TenantIdAndIsDeletedFalse(String roomId, String tenantId);

    @Query("SELECT r.status, COUNT(r) FROM Room r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "GROUP BY r.status")
    List<Object[]> countByStatusGrouped(@Param("tenantId") String tenantId);

    @Query("SELECT r.floor, COUNT(r) FROM Room r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "GROUP BY r.floor ORDER BY r.floor")
    List<Object[]> countByFloorGrouped(@Param("tenantId") String tenantId);

    @Query("SELECT r.roomType.name, COUNT(r) FROM Room r " +
            "WHERE r.tenant.tenantId = :tenantId AND r.isDeleted = false " +
            "GROUP BY r.roomType.name")
    List<Object[]> countByRoomTypeGrouped(@Param("tenantId") String tenantId);

    long countByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    long countByTenant_TenantIdAndStatusAndIsDeletedFalse(String tenantId, RoomStatus status);
}