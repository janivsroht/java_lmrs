package com.project.lmrs.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartnerTableReservationRequest {
    @NotBlank private String guestFirstName;
    @NotBlank private String guestLastName;
    @Email @NotBlank private String guestEmail;
    private String guestPhone;
    @Min(1) @Max(20) private int partySize;
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") private LocalDateTime reservationDateTime;
    private String specialNotes;
}
