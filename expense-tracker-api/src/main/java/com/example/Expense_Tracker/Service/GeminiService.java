package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
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
        if (apiKey == null || apiKey.isBlank())
            throw new AiChatException("Gemini API key is not configured.", 503);

        Map<String, Object> request = Map.of(
                "systemInstruction",
                Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents",
                List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userMessage))
                )),
                "generationConfig",
                Map.of("maxOutputTokens", 2048)
        );

        try {
            Map<?, ?> response = restClientBuilder
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
            throw new AiChatException(
                    "Gemini request failed. Please try again later.",
                    exception.getStatusCode().is5xxServerError() ? 503 : 502,
                    exception
            );

        } catch (RestClientException exception) {
            throw new AiChatException(
                    "Unable to connect to Gemini. Please try again later.",
                    503,
                    exception
            );
        }
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
