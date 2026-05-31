package com.project.lmrs.repository;

import com.project.lmrs.entity.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TableReservationRepository extends JpaRepository<TableReservation, String> {
    List<TableReservation> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<TableReservation> findAllByTable_TableIdAndReservationDtBetweenAndIsDeletedFalse(
            String tableId, LocalDateTime from, LocalDateTime to
    );
    Optional<TableReservation> findByTableResIdAndTenant_TenantIdAndIsDeletedFalse(String tableResId, String tenantId);
}
