package com.project.lmrs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "groq")
public class GroqConfig {
    private String apiKey;
    private String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
    private String model = "llama3-70b-8192";
    private int maxTokens = 1024;
    private double temperature = 0.7;
}
