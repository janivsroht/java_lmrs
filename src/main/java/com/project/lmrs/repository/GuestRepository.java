package com.project.lmrs.repository;

import com.project.lmrs.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, String> {
    Optional<Guest> findByEmailAndIsDeletedFalse(String email);
    Optional<Guest> findByEmailAndTenant_TenantIdAndIsDeletedFalse(String email, String tenantId);
    List<Guest> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<Guest> findAllByTenant_TenantIdAndLastNameContainingIgnoreCaseAndIsDeletedFalse(String tenantId, String lastName);
    Optional<Guest> findByGuestIdAndTenant_TenantIdAndIsDeletedFalse(String guestId, String tenantId);
    long countByTenant_TenantIdAndIsDeletedFalse(String tenantId);

    Page<Guest> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId, Pageable pageable);
    Page<Guest> findAllByTenant_TenantIdAndLastNameContainingIgnoreCaseAndIsDeletedFalse(String tenantId, String lastName, Pageable pageable);
}
