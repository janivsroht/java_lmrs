package com.project.lmrs.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemPointsRequest {

    @NotNull
    @Min(1)
    private Integer points;

    private String referenceId;

    private String referenceType;
}
