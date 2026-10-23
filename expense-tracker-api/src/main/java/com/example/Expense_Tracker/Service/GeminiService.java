
package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiService implements AiProvider {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiService.class);

    private static final int MAX_ATTEMPTS = 2;
    private static final long RETRY_DELAY_MS = 500;

    private final RestClient.Builder restClientBuilder;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.8-flash}")
    private String model;

    public GeminiService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public String generateResponse(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiChatException(
                    "Gemini API key is not configured.", 503
            );
        }

        Map<String, Object> request = Map.of(
                "systemInstruction",
                Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents",
                List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userMessage))
                )),
                "generationConfig",
                Map.of(
                        "maxOutputTokens", 2048,
                        "thinkingConfig", Map.of(
                                "thinkingLevel", "low"
                        )
                )
        );

        long startedAt = System.nanoTime();

        try {
            Map<?, ?> response = executeRequest(request);

            String answer = extractText(response);

            if (answer == null || answer.isBlank()) {
                throw new AiChatException(
                        "Gemini returned an empty response.", 503
                );
            }

            return answer.trim();

        } catch (AiChatException exception) {
            throw exception;

        } catch (RestClientResponseException exception) {
            log.warn(
                    "Gemini API returned HTTP status {}. Response body: {}",
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString()
            );

            throw new AiChatException(
                    "Gemini request failed. Please try again later.",
                    exception.getStatusCode().is5xxServerError() ? 503 : 502,
                    exception
            );

        } catch (RestClientException exception) {
            log.warn(
                    "Gemini HTTP request failed: {}",
                    exception.toString(),
                    exception
            );

            throw new AiChatException(
                    "Unable to connect to Gemini. Please try again later.",
                    503,
                    exception
            );

        } finally {
            long elapsedMs =
                    (System.nanoTime() - startedAt) / 1_000_000;

            log.info(
                    "Gemini request finished: model={}, elapsedMs={}, "
                            + "systemPromptChars={}, userMessageChars={}",
                    model,
                    elapsedMs,
                    systemPrompt == null ? 0 : systemPrompt.length(),
                    userMessage == null ? 0 : userMessage.length()
            );
        }
    }

    private Map<?, ?> executeRequest(Map<String, Object> request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return restClientBuilder
                        .baseUrl("https://generativelanguage.googleapis.com")
                        .build()
                        .post()
                        .uri(
                                "/v1beta/models/{model}:generateContent",
                                model
                        )
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class);

            } catch (RestClientResponseException exception) {
                boolean isServiceUnavailable =
                        exception.getStatusCode().value() == 503;

                if (!isServiceUnavailable || attempt == MAX_ATTEMPTS) {
                    throw exception;
                }

                log.warn(
                        "Gemini returned HTTP 503. "
                                + "Retrying once after {} ms.",
                        RETRY_DELAY_MS
                );

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();

                    throw new RestClientException(
                            "Interrupted while waiting to retry Gemini.",
                            interruptedException
                    );
                }
            }
        }

        throw new IllegalStateException(
                "Gemini request attempts exhausted."
        );
    }

    private String extractText(Map<?, ?> response) {
        if (response == null) {
            return null;
        }

        Object candidatesObject = response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {
            return null;
        }

        if (!(candidates.get(0) instanceof Map<?, ?> candidate)) {
            return null;
        }

        if (!(candidate.get("content") instanceof Map<?, ?> content)) {
            return null;
        }

        if (!(content.get("parts") instanceof List<?> parts)) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (Object partObject : parts) {
            if (partObject instanceof Map<?, ?> part
                    && part.get("text") instanceof String text) {
                result.append(text);
            }
        }

        return result.toString();
    }
}
