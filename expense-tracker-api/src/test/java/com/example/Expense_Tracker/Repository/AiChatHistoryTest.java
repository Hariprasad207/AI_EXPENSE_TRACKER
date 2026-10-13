package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.AIChatHistory;
import com.example.Expense_Tracker.entity.AIChatHistory.AiRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("test")
class AiChatHistoryRepoTest {

    @Autowired
    private AiChatHistoryRepo aiChatHistoryRepo;

    private AIChatHistory userMessage;
    private AIChatHistory assistantMessage;

    @BeforeEach
    void setUp() {
        aiChatHistoryRepo.deleteAll();

        userMessage = aiChatHistoryRepo.save(
                AIChatHistory.builder()
                        .userId(1L)
                        .conversationId("conversation-1")
                        .role(AiRole.USER)
                        .message("How much did I spend this month?")
                        .createdAt(LocalDate.of(2026, 10, 10))
                        .build()
        );

        assistantMessage = aiChatHistoryRepo.save(
                AIChatHistory.builder()
                        .userId(1L)
                        .conversationId("conversation-1")
                        .role(AiRole.ASSISTANT)
                        .message("Your total spending is 12000.")
                        .createdAt(LocalDate.of(2026, 10, 11))
                        .build()
        );

        aiChatHistoryRepo.save(
                AIChatHistory.builder()
                        .userId(1L)
                        .conversationId("conversation-2")
                        .role(AiRole.USER)
                        .message("Show my budget.")
                        .createdAt(LocalDate.of(2026, 10, 12))
                        .build()
        );

        aiChatHistoryRepo.save(
                AIChatHistory.builder()
                        .userId(2L)
                        .conversationId("conversation-1")
                        .role(AiRole.USER)
                        .message("Show my expenses.")
                        .createdAt(LocalDate.of(2026, 10, 13))
                        .build()
        );
    }

    @Test
    void saveChatMessage_shouldGenerateId() {
        AIChatHistory saved = aiChatHistoryRepo.save(
                AIChatHistory.builder()
                        .userId(3L)
                        .conversationId("conversation-3")
                        .role(AiRole.USER)
                        .message("Hello AI.")
                        .createdAt(LocalDate.now())
                        .build()
        );

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByUserIdAndConversationIdOrderByCreatedAtAsc_shouldReturnConversationChronologically() {
        List<AIChatHistory> messages =aiChatHistoryRepo
                        .findByUserIdAndConversationIdOrderByCreatedAtAsc(
                                1L, "conversation-1");

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo(AiRole.USER);
        assertThat(messages.get(1).getRole()).isEqualTo(AiRole.ASSISTANT);
        assertThat(messages.get(0).getCreatedAt())
                .isBeforeOrEqualTo(messages.get(1).getCreatedAt());
    }

    @Test
    void findByUserIdAndConversationIdOrderByCreatedAtAsc_shouldNotReturnOtherConversations() {
        List<AIChatHistory> messages = aiChatHistoryRepo
                        .findByUserIdAndConversationIdOrderByCreatedAtAsc(
                                1L, "conversation-1");

        assertThat(messages).allMatch(message ->
                        message.getConversationId().equals("conversation-1"));
    }

    @Test
    void findByUserIdAndConversationIdOrderByCreatedAtAsc_shouldNotReturnOtherUsersMessages() {
        List<AIChatHistory> messages =
                aiChatHistoryRepo.findByUserIdAndConversationIdOrderByCreatedAtAsc(
                                1L, "conversation-1");

        assertThat(messages)
                .allMatch(message -> message.getUserId().equals(1L));
    }

    @Test
    void findByUserIdOrderByCreatedAtAsc_shouldReturnAllUsersMessagesChronologically() {
        List<AIChatHistory> messages =
                aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(1L);

        assertThat(messages).hasSize(3);
        assertThat(messages)
                .extracting(AIChatHistory::getCreatedAt)
                .isSorted();
    }

    @Test
    void findByUserIdOrderByCreatedAtAsc_shouldExcludeOtherUsersMessages() {
        List<AIChatHistory> messages = aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(1L);

        assertThat(messages).allMatch(
                message -> message.getUserId().equals(1L));
    }

    @Test
    void deleteByUserIdAndConversationId_shouldDeleteOnlySelectedConversation() {
        aiChatHistoryRepo.deleteByUserIdAndConversationId(1L, "conversation-1");

        List<AIChatHistory> remaining = aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(1L);

        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getConversationId())
                .isEqualTo("conversation-2");
    }

    @Test
    void deleteByUserIdAndConversationId_shouldNotDeleteAnotherUsersMessages() {
        aiChatHistoryRepo.deleteByUserIdAndConversationId(1L, "conversation-1");

        List<AIChatHistory> otherUserMessages = aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(2L);

        assertThat(otherUserMessages).hasSize(1);
        assertThat(otherUserMessages.get(0).getMessage())
                .isEqualTo("Show my expenses.");
    }

    @Test
    void deleteByUserId_shouldDeleteOnlySelectedUsersMessages() {
        aiChatHistoryRepo.deleteByUserId(1L);

        assertThat(aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(1L))
                .isEmpty();

        assertThat(aiChatHistoryRepo.findByUserIdOrderByCreatedAtAsc(2L))
                .hasSize(1);
    }
}