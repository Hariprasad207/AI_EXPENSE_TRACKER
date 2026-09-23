package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
import com.example.Expense_Tracker.dto.AiChat.OllamaChatResponse;
import com.example.Expense_Tracker.dto.AiChat.OllamachatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OllamaService {
    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private final RestClient.Builder restClientBuilder;

    public String generateResponse(String systemPrompt, String userMessage) {

        OllamachatRequest request = OllamachatRequest.builder()
                .model(ollamaModel)
                .stream(false)
                .think(false)
                .messages(List.of(
                        OllamachatRequest.Message.builder()
                                .role("system")
                                .content(systemPrompt)
                                .build(),

                        OllamachatRequest.Message.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build();
        try {

            OllamaChatResponse response = restClientBuilder
                    .baseUrl(ollamaBaseUrl)
                    .build()
                    .post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            if (response == null ||
                    response.getMessage() == null ||
                    response.getMessage().getContent() == null) {
                throw new AiChatException("No valid response received from Ollama",503);
            }
            String cleanedResponse = cleanResponse(response.getMessage().getContent());

            if (cleanedResponse == null ||cleanedResponse.isBlank()) {
                throw new AiChatException("AI service returned an empty response.",503);
            }
            return cleanedResponse;
        }
        catch (AiChatException exception) {
            throw exception;
        }
    }
    private String cleanResponse(String response) {

        if (response == null || response.isBlank()) {
            return response;
        }

        return response
                .replaceAll("(?s)<think>.*?</think>", "")
                .trim();
    }
}

