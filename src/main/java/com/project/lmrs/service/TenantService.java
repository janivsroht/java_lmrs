package com.project.lmrs.service;

import com.project.lmrs.entity.Tenant;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAllByIsDeletedFalse();
    }

    public Tenant getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
    }

    public Tenant getTenantBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "subdomain", subdomain));
    }

    @Transactional
    public Tenant createTenant(String name, String subdomain, Map<String, Object> configJson) {
        if (tenantRepository.existsBySubdomain(subdomain)) {
            throw new BusinessRuleException("Subdomain already taken: " + subdomain);
        }

        Tenant tenant = Tenant.builder()
                .name(name)
                .subdomain(subdomain)
                .configJson(configJson)
                .isActive(true)
                .isDeleted(false)
                .build();

        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant updateTenant(String tenantId, String name, Map<String, Object> configJson) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (name != null) tenant.setName(name);
        if (configJson != null) tenant.setConfigJson(configJson);

        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        tenant.setDeleted(true);
        tenant.setDeletedAt(java.time.LocalDateTime.now());
        tenantRepository.save(tenant);
    }
}
