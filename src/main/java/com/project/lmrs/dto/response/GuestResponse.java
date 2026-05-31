package com.project.lmrs.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GuestResponse {
    private String guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String nationality;
    private String idDocType;
    private String idDocNumber;
    private String loyaltyTier;
}
