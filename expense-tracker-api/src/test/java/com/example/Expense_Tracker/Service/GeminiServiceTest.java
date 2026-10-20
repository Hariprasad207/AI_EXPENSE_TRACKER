
package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiServiceTest {

    private MockRestServiceServer server;
    private GeminiService geminiService;

    private static final String API_KEY = "test-api-key";
    private static final String MODEL = "gemini-3.8-flash";

    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        geminiService = new GeminiService(builder);

        ReflectionTestUtils.setField(
                geminiService, "apiKey", API_KEY
        );
        ReflectionTestUtils.setField(
                geminiService, "model", MODEL
        );
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @Test
    void generateResponse_shouldReturnGeneratedText() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath(
                        "$.systemInstruction.parts[0].text"
                ).value("Financial assistant"))
                .andExpect(jsonPath(
                        "$.contents[0].parts[0].text"
                ).value("Analyze my spending"))
                .andRespond(withSuccess(
                        """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  {
                                    "text": "Your spending increased."
                                  }
                                ],
                                "role": "model"
                              },
                              "finishReason": "STOP"
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        String result = geminiService.generateResponse(
                "Financial assistant",
                "Analyze my spending"
        );

        assertEquals("Your spending increased.", result);
    }

    @Test
    void generateResponse_shouldCombineMultipleTextParts() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(
                        """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [
                                  {"text": "Your spending "},
                                  {"text": "increased."}
                                ]
                              }
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        String result = geminiService.generateResponse(
                "System prompt", "Analyze spending"
        );

        assertEquals("Your spending increased.", result);
    }

    @Test
    void generateResponse_shouldRejectMissingApiKey() {
        ReflectionTestUtils.setField(
                geminiService, "apiKey", ""
        );

        assertThrows(
                AiChatException.class,
                () -> geminiService.generateResponse(
                        "System prompt", "Hello"
                )
        );
    }

    @Test
    void generateResponse_shouldRejectEmptyResponse() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(
                        """
                        {"candidates":[]}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                AiChatException.class,
                () -> geminiService.generateResponse(
                        "System prompt", "Hello"
                )
        );
    }

    @Test
    void generateResponse_shouldConvertHttpErrorToAiChatException() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        AiChatException exception = assertThrows(
                AiChatException.class,
                () -> geminiService.generateResponse(
                        "System prompt", "Hello"
                )
        );

        assertTrue(
                exception.getMessage().contains("Gemini request failed")
        );
    }

    @Test
    void generateResponse_shouldNotIncludeApiKeyInRequestBody() {
        server.expect(requestTo(URL))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(API_KEY)
                        )
                ))
                .andRespond(withSuccess(
                        """
                        {
                          "candidates": [
                            {
                              "content": {
                                "parts": [{"text": "OK"}]
                              }
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertEquals(
                "OK",
                geminiService.generateResponse("System", "Hello")
        );
    }
}
