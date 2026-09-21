package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaTestController {

    private final OllamaService ollamaService;

    @PostMapping("/test")
    public ResponseEntity<Map<String ,String>> testOllama(@RequestBody Map<String,String> request){
        String userMessage = request.get("message");
        if(userMessage==null || userMessage.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("error","Message Cannot be Empyt"));

        String systemPrompt = """
                You are a helpful financial assistant.
                Answer the user's question clearly and concisely.
                Do not invent financial data.
                """;
        String response = ollamaService.generateResponse(systemPrompt,userMessage);
        return ResponseEntity.ok(Map.of("response",response));
    }
}
