package com.project.lmrs.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdatePartnerReservationRequest {
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate checkInDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate checkOutDate;
    private String specialRequests;
    private String roomTypeId;
}
