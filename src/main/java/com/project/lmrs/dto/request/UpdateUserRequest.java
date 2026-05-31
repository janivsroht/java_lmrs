package com.project.lmrs.dto.request;

import com.project.lmrs.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private UserRole role;
}
