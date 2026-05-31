package com.project.lmrs.repository;

import com.project.lmrs.entity.Folio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FolioRepository extends JpaRepository<Folio, String> {
    @EntityGraph(attributePaths = {"lineItems", "payments", "guest", "reservation"})
    Optional<Folio> findByReservation_ReservationIdAndIsDeletedFalse(String reservationId);

    @EntityGraph(attributePaths = {"lineItems", "payments", "guest", "reservation"})
    Optional<Folio> findByFolioIdAndIsDeletedFalse(String folioId);

    @EntityGraph(attributePaths = {"lineItems", "payments", "guest", "reservation"})
    Optional<Folio> findByFolioIdAndReservation_Tenant_TenantIdAndIsDeletedFalse(String folioId, String tenantId);

    Optional<Folio> findByGuest_GuestIdAndStatusAndIsDeletedFalse(String guestId, String status);
}
