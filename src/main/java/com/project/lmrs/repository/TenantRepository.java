package com.project.lmrs.repository;

import com.project.lmrs.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findBySubdomain(String subdomain);
    boolean existsBySubdomain(String subdomain);
    List<Tenant> findAllByIsDeletedFalse();
}