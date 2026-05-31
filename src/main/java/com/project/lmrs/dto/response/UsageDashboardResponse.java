package com.project.lmrs.dto.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsageDashboardResponse {
    private String partnerId;
    private String partnerName;
    private String providerType;
    private long totalCalls;
    private double avgResponseMs;
    private long successCalls;
    private long errorCalls;
    private List<EndpointStat> endpointStats;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EndpointStat {
        private String endpoint;
        private long callCount;
        private double avgResponseMs;
    }
}
