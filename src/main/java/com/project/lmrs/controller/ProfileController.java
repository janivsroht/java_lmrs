package com.project.lmrs.controller;

import com.project.lmrs.dto.request.ChangePasswordRequest;
import com.project.lmrs.entity.User;
import com.project.lmrs.security.SecurityUtils;
import com.project.lmrs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(Map.of("userId", userId, "email", email));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
