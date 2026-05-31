package com.project.lmrs.service;

import com.project.lmrs.dto.request.LoginRequest;
import com.project.lmrs.dto.request.RegisterRequest;
import com.project.lmrs.dto.response.AuthResponse;
import com.project.lmrs.entity.Tenant;
import com.project.lmrs.entity.User;
import com.project.lmrs.enums.UserRole;
import com.project.lmrs.exception.BusinessRuleException;
import com.project.lmrs.exception.ResourceNotFoundException;
import com.project.lmrs.repository.TenantRepository;
import com.project.lmrs.repository.UserRepository;
import com.project.lmrs.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        // Authenticate — throws exception if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Update last login
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenant().getTenantId())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new BusinessRuleException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessRuleException("Invalid token type for refresh");
        }

        String email = jwtTokenProvider.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isActive()) {
            throw new BusinessRuleException("Account is deactivated");
        }

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenant().getTenantId())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered: " + request.getEmail());
        }

        if (request.getRole() != UserRole.FRONT_DESK) {
            throw new BusinessRuleException("Self-registration is only allowed with FRONT_DESK role");
        }

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", request.getTenantId()));

        User user = User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .isDeleted(false)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(tenant.getTenantId())
                .build();
    }
}