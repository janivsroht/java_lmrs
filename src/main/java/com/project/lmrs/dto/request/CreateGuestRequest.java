package com.project.lmrs.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateGuestRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    private String email;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private String nationality;

    private String idDocType;

    private String idDocNumber;
}
