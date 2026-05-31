package com.project.lmrs.service;

import com.project.lmrs.entity.Tenant;
import com.project.lmrs.entity.User;
import com.project.lmrs.enums.UserRole;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.exception.UnauthorizedException;
import com.project.lmrs.repository.TenantRepository;
import com.project.lmrs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, tenantRepository, passwordEncoder);
    }

    @Test
    void createUser_shouldSucceed() {
        String tenantId = "t1";
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(tenantId, "test@test.com", "pass123", UserRole.FRONT_DESK);

        assertEquals("test@test.com", result.getEmail());
        assertEquals("encoded", result.getPasswordHash());
        assertEquals(UserRole.FRONT_DESK, result.getRole());
        assertTrue(result.isActive());
        verify(userRepository).save(any());
    }

    @Test
    void createUser_duplicateEmail_shouldThrow() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> userService.createUser("t1", "dup@test.com", "pass", UserRole.FRONT_DESK));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldSucceed() {
        String userId = "u1";
        User user = User.builder().userId(userId).passwordHash("oldEncoded").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        userService.changePassword(userId, "oldPass", "newPass");

        assertEquals("newEncoded", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongCurrent_shouldThrow() {
        String userId = "u1";
        User user = User.builder().userId(userId).passwordHash("oldEncoded").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "oldEncoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> userService.changePassword(userId, "wrongPass", "newPass"));
    }

    @Test
    void getUserById_shouldSucceed() {
        String tenantId = "t1";
        User user = User.builder().userId("u1").email("test@test.com").build();

        when(userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse("u1", tenantId))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById("u1", tenantId);
        assertEquals("u1", result.getUserId());
    }

    @Test
    void getUserById_notFound_shouldThrow() {
        when(userRepository.findByUserIdAndTenant_TenantIdAndIsDeletedFalse("u1", "t1"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById("u1", "t1"));
    }
}
