
package com.example.Expense_Tracker.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resourceNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Expense not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidRequest_shouldReturn400() throws Exception {
        String request = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(
                        post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("name:")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void aiChatException_shouldReturnConfiguredStatus() throws Exception {
        mockMvc.perform(get("/test/ai-error"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message")
                        .value("AI service unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void badRequestException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid expense data"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unexpectedException_shouldReturnGeneric500() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error is occurred. Please try again later"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("database")
                        )))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    static class TestController {

        @org.springframework.web.bind.annotation.GetMapping("/test/not-found")
        String notFound() {
            throw new ResourceNotFoundException("Expense not found");
        }

        @PostMapping("/test/validation")
        String validation(@Valid @RequestBody ValidationRequest request) {
            return "Valid";
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/ai-error")
        String aiError() {
            throw new AiChatException("AI service unavailable", 503);
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/bad-request")
        String badRequest() {
            throw new BadRequestException("Invalid expense data");
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/unexpected-error")
        String unexpectedError() {
            throw new IllegalStateException("Internal database details");
        }
    }

    static class ValidationRequest {
        @NotBlank(message = "Name is required")
        public String name;
    }
}
