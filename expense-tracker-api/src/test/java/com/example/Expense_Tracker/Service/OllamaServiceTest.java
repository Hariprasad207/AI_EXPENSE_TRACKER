package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OllamaServiceTest {

    private MockRestServiceServer server;
    private OllamaService ollamaService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        ollamaService = new OllamaService(builder);

        ReflectionTestUtils.setField(
                ollamaService, "ollamaBaseUrl",
                "http://localhost:11434"
        );
        ReflectionTestUtils.setField(
                ollamaService, "ollamaModel", "qwen3:1.7b"
        );
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @Test
    void generateResponse_shouldReturnAssistantMessage() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("qwen3:1.7b"))
                .andExpect(jsonPath("$.stream").value(false))
                .andRespond(withSuccess(
                        """
                        {
                          "message": {
                            "role": "assistant",
                            "content": "Your expenses are within budget."
                          },
                          "done": true
                        }
                        """,
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        String result = ollamaService.generateResponse(
                "You are a financial assistant.",
                "Analyze my expenses."
        );

        assertEquals(
                "Your expenses are within budget.",
                result
        );
    }

    @Test
    void generateResponse_shouldRemoveThinkTags() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(
                        """
                        {
                          "message": {
                            "role": "assistant",
                            "content": "<think>internal reasoning</think> Your spending increased."
                          },
                          "done": true
                        }
                        """,
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        String result = ollamaService.generateResponse(
                "System prompt",
                "Analyze spending."
        );

        assertEquals("Your spending increased.", result);
    }

    @Test
    void generateResponse_shouldRejectEmptyAssistantMessage() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(
                        """
                        {
                          "message": {
                            "role": "assistant",
                            "content": ""
                          },
                          "done": true
                        }
                        """,
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        assertThrows(
                AiChatException.class,
                () -> ollamaService.generateResponse(
                        "System prompt", "Hello"
                )
        );
    }


    @Test
    void generateResponse_shouldRejectMissingMessage() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(
                        """
                        {
                          "done": true,
                          "message": null
                        }
                        """,
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        assertThrows(
                AiChatException.class,
                () -> ollamaService.generateResponse("System prompt", "Hello")
        );
    }


    @Test
    void generateResponse_shouldPropagateHttpError() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(
                RestClientResponseException.class,
                () -> ollamaService.generateResponse(
                        "System prompt", "Hello"
                )
        );
    }
}
