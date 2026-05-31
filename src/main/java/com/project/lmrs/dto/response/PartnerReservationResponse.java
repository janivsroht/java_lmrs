package com.project.lmrs.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartnerReservationResponse {
    private String reservationId;
    private String guestName;
    private String guestEmail;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private BigDecimal rateApplied;
    private String specialRequests;
    private String bookingChannel;
}
