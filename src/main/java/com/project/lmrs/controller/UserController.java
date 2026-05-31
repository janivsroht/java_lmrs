package com.project.lmrs.controller;

import com.project.lmrs.dto.request.CreateUserRequest;
import com.project.lmrs.dto.request.UpdateUserRequest;
import com.project.lmrs.dto.response.UserResponse;
import com.project.lmrs.entity.User;
import com.project.lmrs.enums.UserRole;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<User> users = userService.getAllUsers(tenantId);
        return ResponseEntity.ok(users.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        User user = userService.getUserById(userId, tenantId);
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        User user = userService.createUser(tenantId, request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        User user = userService.updateUser(userId, request.getEmail(), request.getRole(), tenantId);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable String userId, @RequestParam UserRole role) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        User user = userService.updateUserRole(userId, role, tenantId);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{userId}/toggle-active")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> toggleActive(@PathVariable String userId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        User user = userService.toggleActiveStatus(userId, tenantId);
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('PROPERTY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        userService.deleteUser(userId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
