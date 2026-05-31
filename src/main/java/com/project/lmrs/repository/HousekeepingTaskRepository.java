package com.project.lmrs.repository;

import com.project.lmrs.entity.HousekeepingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, String> {
    List<HousekeepingTask> findAllByTenant_TenantIdAndScheduledDateAndIsDeletedFalse(String tenantId, LocalDate date);
    List<HousekeepingTask> findAllByAssignedUserIdAndScheduledDateAndIsDeletedFalse(String userId, LocalDate date);
    List<HousekeepingTask> findAllByAssignedUserIdAndTenant_TenantIdAndScheduledDateAndIsDeletedFalse(String userId, String tenantId, LocalDate date);
    List<HousekeepingTask> findAllByRoom_RoomIdAndIsDeletedFalse(String roomId);

    @Query("SELECT h.status, COUNT(h) FROM HousekeepingTask h " +
            "WHERE h.tenant.tenantId = :tenantId AND h.isDeleted = false " +
            "GROUP BY h.status")
    List<Object[]> countByStatusGrouped(@Param("tenantId") String tenantId);

    long countByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    long countByTenant_TenantIdAndScheduledDateAndIsDeletedFalse(String tenantId, LocalDate date);

    @Query("SELECT COUNT(h) FROM HousekeepingTask h " +
            "WHERE h.tenant.tenantId = :tenantId AND h.isDeleted = false " +
            "AND h.status = 'COMPLETED' AND h.scheduledDate = :date")
    long countCompletedByDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    Optional<HousekeepingTask> findByTaskIdAndTenant_TenantIdAndIsDeletedFalse(String taskId, String tenantId);
}
