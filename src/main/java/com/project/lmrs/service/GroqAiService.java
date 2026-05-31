package com.project.lmrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.lmrs.config.GroqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroqAiService {

    private final GroqConfig groqConfig;
    private final AuditLogService auditLogService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> chatCompletion(String systemPrompt, String userMessage) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", groqConfig.getModel());
            body.put("max_tokens", groqConfig.getMaxTokens());
            body.put("temperature", groqConfig.getTemperature());

            ArrayNode messages = body.putArray("messages");
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqConfig.getApiKey());

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    groqConfig.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            if (responseBody == null) {
                return Map.of("content", "No response from AI service", "model", groqConfig.getModel(), "tokens", 0);
            }

            String content = responseBody.path("choices").get(0).path("message").path("content").asText("");
            String model = responseBody.path("model").asText(groqConfig.getModel());
            int tokens = responseBody.path("usage").path("total_tokens").asInt(0);

            // Audit the AI call
            try {
                auditLogService.log(
                        null,
                        null,
                        "AI_CHAT_COMPLETION",
                        "GroqAiService",
                        null,
                        Map.of("userMessage", userMessage.length() > 200 ? userMessage.substring(0, 200) + "..." : userMessage),
                        Map.of("contentLength", content.length(), "tokens", tokens),
                        null
                );
            } catch (Exception ignored) {}

            return Map.of("content", content, "model", model, "tokens", tokens);

        } catch (Exception e) {
            return Map.of("content", "AI service error: " + e.getMessage(), "model", groqConfig.getModel(), "tokens", 0);
        }
    }
}
