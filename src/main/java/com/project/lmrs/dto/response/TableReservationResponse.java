package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TableReservationResponse {
    private String tableResId;
    private String tableId;
    private String tableNumber;
    private String guestId;
    private String guestName;
    private int partySize;
    private LocalDateTime reservationDt;
    private String status;
    private String specialNotes;
}
