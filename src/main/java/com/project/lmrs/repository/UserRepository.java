package com.project.lmrs.repository;

import com.project.lmrs.entity.User;
import com.project.lmrs.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findAllByTenant_TenantIdAndIsDeletedFalse(String tenantId);
    List<User> findAllByTenant_TenantIdAndRoleAndIsDeletedFalse(String tenantId, UserRole role);
    boolean existsByEmail(String email);
    boolean existsByEmailAndUserIdNot(String email, String userId);
    Optional<User> findByUserIdAndTenant_TenantIdAndIsDeletedFalse(String userId, String tenantId);
}
