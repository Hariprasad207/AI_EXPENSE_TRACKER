package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.AIChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatHistoryRepo extends JpaRepository<AIChatHistory,Long> {
    List<AIChatHistory> findByUserIdAndConversationIdOrderByCreatedAtAsc(Long userId,String conversationId);
    List<AIChatHistory> findByUserIdOrderByCreatedAtAsc(Long userId);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndConversationId(Long userId,String conversationId);
}
