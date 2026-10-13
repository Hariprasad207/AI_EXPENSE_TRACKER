package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.AiInsight;
import com.example.Expense_Tracker.entity.AiInsight.InsightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("test")
class AiInsightRepoTest {

    @Autowired
    private AiInsightRepo aiInsightRepo;

    private AiInsight unreadInsight;
    private AiInsight readInsight;

    @BeforeEach
    void setUp() {
        aiInsightRepo.deleteAll();

        unreadInsight = aiInsightRepo.save(
                AiInsight.builder()
                        .userId(1L)
                        .title("High Spending")
                        .insightKey("HIGH_SPENDING_OCT_2026")
                        .message("Your spending is higher than usual.")
                        .type(InsightType.HIGH_SPENDING)
                        .read(false)
                        .month(10)
                        .year(2026)
                        .createdAt(OffsetDateTime.parse(
                                "2026-10-10T10:00:00+05:30"
                        ))
                        .build()
        );

        readInsight = aiInsightRepo.save(
                AiInsight.builder()
                        .userId(1L)
                        .title("Saving Opportunity")
                        .insightKey("SAVING_OCT_2026")
                        .message("Consider reducing discretionary expenses.")
                        .type(InsightType.SAVING)
                        .read(true)
                        .month(10)
                        .year(2026)
                        .createdAt(OffsetDateTime.parse(
                                "2026-10-11T10:00:00+05:30"
                        ))
                        .build()
        );

        aiInsightRepo.save(
                AiInsight.builder()
                        .userId(2L)
                        .title("Budget Warning")
                        .insightKey("BUDGET_WARNING_OCT_2026")
                        .message("You are approaching your monthly budget.")
                        .type(InsightType.BUDGET_WARNING)
                        .read(false)
                        .month(10)
                        .year(2026)
                        .createdAt(OffsetDateTime.parse(
                                "2026-10-12T10:00:00+05:30"
                        ))
                        .build()
        );
    }

    @Test
    void saveInsight_shouldGenerateIdAndCreatedAt() {
        AiInsight insight = aiInsightRepo.save(
                AiInsight.builder()
                        .userId(3L)
                        .title("General Insight")
                        .insightKey("GENERAL_TEST")
                        .message("This is a test insight.")
                        .type(InsightType.GENERAL)
                        .build()
        );

        assertThat(insight.getId()).isNotNull();
        assertThat(insight.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnNewestFirst() {
        List<AiInsight> insights =
                aiInsightRepo.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(insights).hasSize(2);
        assertThat(insights.get(0).getTitle())
                .isEqualTo("Saving Opportunity");
        assertThat(insights.get(1).getTitle())
                .isEqualTo("High Spending");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnOnlyUsersInsights() {
        List<AiInsight> insights =
                aiInsightRepo.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(insights)
                .allMatch(insight -> insight.getUserId().equals(1L));
    }

    @Test
    void findByIdAndUserId_shouldReturnInsightForCorrectUser() {
        Optional<AiInsight> result =
                aiInsightRepo.findByIdAndUserId(
                        unreadInsight.getId(), 1L
                );

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("High Spending");
    }

    @Test
    void findByIdAndUserId_shouldNotReturnAnotherUsersInsight() {
        Optional<AiInsight> result =
                aiInsightRepo.findByIdAndUserId(
                        unreadInsight.getId(), 2L
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndReadFalseOrderByCreatedAtDesc_shouldReturnUnreadOnly() {
        List<AiInsight> insights =
                aiInsightRepo.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);

        assertThat(insights).hasSize(1);
        assertThat(insights.get(0).getTitle()).isEqualTo("High Spending");
        assertThat(insights)
                .allMatch(insight -> !insight.isRead());
    }

    @Test
    void countByUserIdAndReadFalse_shouldCountOnlyUnreadInsights() {
        long count = aiInsightRepo.countByUserIdAndReadFalse(1L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsByUserIdAndInsightKeyAndMonthAndYear_shouldReturnTrueForExistingInsight() {
        boolean exists =
                aiInsightRepo.existsByUserIdAndInsightKeyAndMonthAndYear(
                        1L, "HIGH_SPENDING_OCT_2026", 10, 2026
                );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndInsightKeyAndMonthAndYear_shouldReturnFalseForDifferentMonth() {
        boolean exists =
                aiInsightRepo.existsByUserIdAndInsightKeyAndMonthAndYear(
                        1L, "HIGH_SPENDING_OCT_2026", 9, 2026
                );

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserIdAndInsightKeyAndMonthAndYear_shouldReturnMatchingInsight() {
        Optional<AiInsight> result =
                aiInsightRepo.findByUserIdAndInsightKeyAndMonthAndYear(
                        1L, "HIGH_SPENDING_OCT_2026", 10, 2026
                );

        assertThat(result).isPresent();
        assertThat(result.get().getType())
                .isEqualTo(InsightType.HIGH_SPENDING);
    }

    @Test
    void findByUserIdAndInsightKeyAndMonthAndYear_shouldReturnEmptyWhenNoMatchExists() {
        Optional<AiInsight> result =
                aiInsightRepo.findByUserIdAndInsightKeyAndMonthAndYear(
                        1L, "UNKNOWN_INSIGHT", 10, 2026
                );

        assertThat(result).isEmpty();
    }
}