package com.project.lmrs.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateReservationRequest {

    @NotBlank
    private String guestId;

    @NotBlank
    private String roomId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @NotNull
    private BigDecimal rateApplied;

    private String channel;

    private String specialRequests;

    @AssertTrue(message = "Check-in date must be before check-out date")
    private boolean isDateRangeValid() {
        if (checkInDate == null || checkOutDate == null) return true;
        return checkInDate.isBefore(checkOutDate);
    }
}
