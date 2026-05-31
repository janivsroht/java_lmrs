package com.project.lmrs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.flyway")
public class FlywayConfig {

    private boolean enabled = true;
    private String[] locations = {"classpath:db/migration"};
    private boolean baselineOnMigrate = false;
}
