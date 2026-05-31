package com.project.lmrs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CreateTenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String subdomain;

    private Map<String, Object> configJson;
}
