package com.project.lmrs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantResponse {
    private String tenantId;
    private String name;
    private String subdomain;
    private Map<String, Object> configJson;
    private boolean isActive;
}
