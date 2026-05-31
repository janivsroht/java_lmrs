package com.project.lmrs.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PartnerReservationRequest {
    @NotBlank private String guestFirstName;
    @NotBlank private String guestLastName;
    @Email @NotBlank private String guestEmail;
    private String guestPhone;
    @NotBlank private String roomTypeId;
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate checkInDate;
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate checkOutDate;
    private String specialRequests;
}
