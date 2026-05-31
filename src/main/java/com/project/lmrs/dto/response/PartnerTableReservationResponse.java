package com.project.lmrs.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartnerTableReservationResponse {
    private String tableReservationId;
    private String guestName;
    private String guestEmail;
    private String tableNumber;
    private int partySize;
    private LocalDateTime reservationDateTime;
    private String status;
    private String specialNotes;
}
