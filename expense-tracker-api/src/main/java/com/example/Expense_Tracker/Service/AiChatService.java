package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.AiChatException;
import com.example.Expense_Tracker.Repository.AiChatHistoryRepo;
import com.example.Expense_Tracker.dto.AiChat.AiChatHistoryRequest;
import com.example.Expense_Tracker.dto.AiChat.AiChatHistoryResponse;
import com.example.Expense_Tracker.dto.AiChat.FinancialContext;
import com.example.Expense_Tracker.entity.AIChatHistory;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AiChatService {

    private final AiChatHistoryRepo aiChatHistoryRepo;
    private final FinancialContextService financialContextService;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper;

    public AiChatHistoryResponse chat(Long userId,AiChatHistoryRequest request) {

        String conversationId = request.getConversationId();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new AiChatException("Message cannot be empty",400);
        }

        AIChatHistory userMessage = AIChatHistory.builder()
                .userId(userId)
                .role(AIChatHistory.AiRole.USER)
                .conversationId(conversationId)
                .message(request.getMessage())
                .createdAt(LocalDate.now())
                .build();

        aiChatHistoryRepo.save(userMessage);

        FinancialContext financialContext;

        try {
            financialContext =financialContextService.buildFinancialContext(userId);
        } catch (Exception exception) {
            throw new AiChatException("Unable to retrieve your financial information. Please try again later.",
                    500,exception
            );
        }

        String financialContextJson;

        try {
            financialContextJson =objectMapper.writeValueAsString(financialContext);
        } catch (Exception exception) {
            throw new AiChatException("Unable to process your financial information.",500,exception);
        }

        String systemPrompt = """
                You are a personal financial assistant
                inside a finance tracking application.

                Your responsibilities:
                - Analyze the financial data provided.
                - Answer concisely in 3–5 short points.
                - Do not show your reasoning or internal analysis.
                - Use only the financial data provided.
                - Do not repeat the financial context.
                - Answer the user's financial questions clearly.
                - Use only the financial data provided in the context.
                - Never invent income, expenses, budgets, or transactions.
                - If the requested information is unavailable,
                  clearly state that it is unavailable.
                - Give practical and understandable explanations.
                - Do not provide guaranteed investment returns.
                - Do not make decisions on behalf of the user.

                Financial context:
                %s
                """.formatted(financialContextJson);

        String assistantMessage =
                ollamaService.generateResponse(
                        systemPrompt,
                        request.getMessage()
                );

        AIChatHistory assistantMessageEntity =
                AIChatHistory.builder()
                        .userId(userId)
                        .conversationId(conversationId)
                        .role(AIChatHistory.AiRole.ASSISTANT)
                        .message(assistantMessage)
                        .createdAt(LocalDate.now())
                        .build();

        aiChatHistoryRepo.save(assistantMessageEntity);

        return AiChatHistoryResponse.builder()
                .message(assistantMessage)
                .conversationId(conversationId)
                .build();
    }


    @Transactional(readOnly = true)
    public List<AIChatHistory> getChatHistory(Long userId) {

        return aiChatHistoryRepo
                .findByUserIdOrderByCreatedAtAsc(userId);
    }


    @Transactional(readOnly = true)
    public List<AIChatHistory> getConversation(Long userId,String conversationId) {

        return aiChatHistoryRepo.findByUserIdAndConversationIdOrderByCreatedAtAsc(
                        userId,
                        conversationId
                );
    }


    public void clearChatHistory(Long userId) {
        aiChatHistoryRepo.deleteByUserId(userId);
    }


    public void clearConversation(Long userId,String conversationId) {
        aiChatHistoryRepo.deleteByUserIdAndConversationId(
                userId,
                conversationId
        );
    }
}