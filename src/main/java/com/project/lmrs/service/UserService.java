package com.project.lmrs.service;

import com.project.lmrs.entity.Tenant;
import com.project.lmrs.entity.User;
import com.project.lmrs.enums.UserRole;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.exception.UnauthorizedException;
import com.project.lmrs.repository.TenantRepository;
import com.project.lmrs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers(String tenantId) {
        return userRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenantId);
    }

    public User getUserById(String userId, String tenantId) {
        return userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public List<User> getUsersByRole(String tenantId, UserRole role) {
        return userRepository.findAllByTenant_TenantIdAndRoleAndIsDeletedFalse(tenantId, role);
    }

    @Transactional
    public User createUser(String tenantId, String email, String password, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email already registered: " + email);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        User user = User.builder()
                .tenant(tenant)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .isDeleted(false)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(String userId, UserRole role, String tenantId) {
        User user = userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String userId, String email, UserRole role, String tenantId) {
        User user = userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (userRepository.existsByEmailAndUserIdNot(email, userId)) {
            throw new BusinessRuleException("Email already registered: " + email);
        }

        user.setEmail(email);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleActiveStatus(String userId, String tenantId) {
        User user = userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId, String tenantId) {
        User user = userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
}
