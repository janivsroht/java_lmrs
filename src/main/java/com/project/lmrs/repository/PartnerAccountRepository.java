package com.project.lmrs.repository;

import com.project.lmrs.entity.PartnerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartnerAccountRepository extends JpaRepository<PartnerAccount, String> {
    Optional<PartnerAccount> findByApiKeyAndIsActiveTrueAndIsDeletedFalse(String apiKey);
    List<PartnerAccount> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
}
