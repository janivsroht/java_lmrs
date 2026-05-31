package com.project.lmrs.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReservationResponse {
    private String reservationId;
    private String guestId;
    private String guestName;
    private String roomId;
    private String roomNumber;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private String channel;
    private BigDecimal rateApplied;
    private String specialRequests;
}
