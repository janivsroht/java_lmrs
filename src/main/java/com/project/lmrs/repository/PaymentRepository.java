package com.project.lmrs.repository;

import com.project.lmrs.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findAllByFolio_FolioId(String folioId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.folio.reservation.tenant.tenantId = :tenantId " +
            "AND p.status = 'COMPLETED' AND p.isDeleted = false")
    BigDecimal sumCompletedPaymentsByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT FUNCTION('DATE', p.paidAt), COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.folio.reservation.tenant.tenantId = :tenantId " +
            "AND p.status = 'COMPLETED' AND p.isDeleted = false " +
            "AND p.paidAt BETWEEN :from AND :to " +
            "GROUP BY FUNCTION('DATE', p.paidAt) ORDER BY FUNCTION('DATE', p.paidAt)")
    List<Object[]> paymentsByDateRange(@Param("tenantId") String tenantId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    long countByStatusAndIsDeletedFalse(String status);
}