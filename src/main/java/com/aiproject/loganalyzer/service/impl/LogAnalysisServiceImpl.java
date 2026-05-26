package com.aiproject.loganalyzer.service.impl;

import com.aiproject.loganalyzer.dto.LogRequest;
import com.aiproject.loganalyzer.dto.LogResponse;
import com.aiproject.loganalyzer.service.LogAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisServiceImpl implements LogAnalysisService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public LogResponse analyze(LogRequest request) {
        String cacheKey = buildCacheKey(request);
        LogResponse cachedResponse = (LogResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            log.info("Redis cache hit");
            return cachedResponse;
        }
        log.info("Redis cache miss");
        log.info("Log analysis request received");
        String prompt = buildPrompt(request);
        Map<String, Object> body = buildRequestBody(prompt);
        log.info("Calling Gemini API");
        Map<String, Object> response = callGemini(body);
        log.info("Gemini response received");
        String aiText = extractGeminiText(response);

        LogResponse result = buildSuccessResponse(aiText);
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(10));
        log.info("Response cached in Redis");
        return result;
    }

    private String buildPrompt(LogRequest request) {

        return """
                You are an expert backend production log analyzer.
                
                Analyze the application logs.
                
                STRICT RULES:
                1. Return ONLY valid JSON.
                2. No markdown.
                3. No explanations outside JSON.
                4. Follow EXACT schema below.
                5. anomalySummary max 15 words.
                6. rootCause: 2–4 concise bullets, max 10 words each.
                7. recommendation: 2–4 concise actions, max 10 words each.
                8. Avoid long explanations.
                
                Required JSON schema:
                
                {
                  "anomalySummary": "string",
                  "rootCause": [
                    "string"
                  ],
                  "recommendation": [
                    "string"
                  ]
                }
                
                Application Logs:
                %s
                """.formatted(request.getLogs());
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of("contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", prompt)})});
    }

    private Map<String, Object> callGemini(Map<String, Object> body) {
        log.info("Invoking Gemini model: gemini-2.5-flash");
        return webClient.post().uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey).bodyValue(body).retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        }).block();
    }

    @SuppressWarnings("unchecked")
    private String extractGeminiText(Map<String, Object> response) {

        if (response == null) {
            throw new RuntimeException("Empty response from Gemini");
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Invalid Gemini response");
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Invalid Gemini response");
        }

        Object text = parts.get(0).get("text");
        return text != null ? text.toString() : "";
    }

    private LogResponse buildSuccessResponse(String aiText) {

        try {
            return objectMapper.readValue(aiText, LogResponse.class);
        } catch (Exception e) {
            log.error("Gemini JSON parsing failed", e);
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private String buildCacheKey(LogRequest request) {
        return "log-analysis:" + request.getLogs().hashCode();
    }
}
