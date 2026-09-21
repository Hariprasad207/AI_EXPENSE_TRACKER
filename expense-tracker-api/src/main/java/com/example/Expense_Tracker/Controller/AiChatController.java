package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.AiChatService;
import com.example.Expense_Tracker.Service.FinancialContextService;
import com.example.Expense_Tracker.dto.AiChat.AiChatHistoryRequest;
import com.example.Expense_Tracker.dto.AiChat.AiChatHistoryResponse;
import com.example.Expense_Tracker.dto.AiChat.FinancialContext;
import com.example.Expense_Tracker.entity.AIChatHistory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;
    private final CurrentUserService currentUserService;
    private final FinancialContextService financialContextService;

    @PostMapping
    public ResponseEntity<AiChatHistoryResponse> chat(@Valid @RequestBody AiChatHistoryRequest request){
        Long userId = currentUserService.getCurrentUserID();
        AiChatHistoryResponse response = aiChatService.chat(userId,request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AIChatHistory>> getChatHistory(){
        Long userId = currentUserService.getCurrentUserID();
        List<AIChatHistory> histories = aiChatService.getChatHistory(userId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<AIChatHistory>> getConversation(@PathVariable String conversationId){
        Long userId = currentUserService.getCurrentUserID();
        List<AIChatHistory> conversation = aiChatService.getConversation(userId,conversationId);
        return ResponseEntity.ok(conversation);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearChatHistory(){
        Long userId = currentUserService.getCurrentUserID();
        aiChatService.clearChatHistory(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> clearOneConversation(@PathVariable String conversationId){
        Long userId = currentUserService.getCurrentUserID();
        aiChatService.clearConversation(userId,conversationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/financial-context")   
    public ResponseEntity<FinancialContext> getFinancialContext() {
        Long userId = currentUserService.getCurrentUserID();
        FinancialContext context = financialContextService.buildFinancialContext(userId);
        return ResponseEntity.ok(context);
    }
}
