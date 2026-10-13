package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Income;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IncomeRepoTest {

    @Autowired
    private IncomeRepo incomeRepo;

    @Autowired
    private EntityManager entityManager;

    private final Long userId = 101L;
    private final Long otherUserId = 202L;

    @BeforeEach
    void setUp() {

        saveIncome(userId, 1L, "25000.00",
                LocalDate.of(2026, 1, 5), "January Salary");

        saveIncome(userId, 1L, "5000.00",
                LocalDate.of(2026, 1, 15), "Freelance Work");

        saveIncome(userId, 2L, "3000.00",
                LocalDate.of(2026, 2, 1), "Investment Return");

        saveIncome(otherUserId, 1L, "50000.00",
                LocalDate.of(2026, 1, 10), "Other User Salary");

        entityManager.flush();
        entityManager.clear();
    }

    private Income saveIncome(
            Long ownerId,
            Long categoryId,
            String amount,
            LocalDate incomeDate,
            String title) {

        return incomeRepo.save(
                Income.builder()
                        .userId(ownerId)
                        .categoryId(categoryId)
                        .amount(new BigDecimal(amount))
                        .incomeDate(incomeDate)
                        .title(title)
                        .source("Test")
                        .description("Test income record")
                        .build()
        );
    }

    @Test
    void shouldSaveIncomeAndGenerateId() {

        Income saved = incomeRepo.saveAndFlush(
                Income.builder()
                        .userId(userId)
                        .categoryId(1L)
                        .amount(new BigDecimal("1500.00"))
                        .incomeDate(LocalDate.of(2026, 3, 1))
                        .title("Bonus")
                        .source("Company")
                        .build()
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindIncomeByIdAndUserId() {

        Income saved = saveIncome(
                userId, 1L, "2000.00",
                LocalDate.of(2026, 3, 5), "Part-time Work"
        );

        Optional<Income> result = incomeRepo.findByIdAndUserId(saved.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Part-time Work");
    }

    @Test
    void shouldNotFindIncomeBelongingToAnotherUser() {

        Income otherUsersIncome = incomeRepo.findAll().stream()
                .filter(income -> income.getUserId().equals(otherUserId))
                .findFirst()
                .orElseThrow();

        Optional<Income> result =incomeRepo.findByIdAndUserId(otherUsersIncome.getId(), userId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenIncomeIdDoesNotExist() {

        Optional<Income> result =
                incomeRepo.findByIdAndUserId(999999L, userId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnOnlyCurrentUsersIncomeWithPagination() {

        Page<Income> page = incomeRepo.findByUserId(
                userId,
                PageRequest.of(
                        0, 2,
                        Sort.by(Sort.Direction.ASC, "incomeDate")
                )
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getTitle())
                .isEqualTo("January Salary");

        assertThat(page.getContent())
                .allMatch(income -> income.getUserId().equals(userId));
    }

    @Test
    void shouldReturnSecondPageOfIncome() {

        Page<Income> page = incomeRepo.findByUserId(
                userId,
                PageRequest.of(
                        1, 2,
                        Sort.by(Sort.Direction.ASC, "incomeDate")
                )
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle())
                .isEqualTo("Investment Return");
    }

    @Test
    void shouldCalculateTotalIncomeForDateRange() {

        // Start date is inclusive; end date is exclusive.
        BigDecimal total = incomeRepo.getTotalIncomeForDateRange(
                userId,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );

        assertThat(total).isEqualByComparingTo("30000.00");
    }

    @Test
    void shouldReturnZeroWhenNoIncomeExistsInDateRange() {

        BigDecimal total = incomeRepo.getTotalIncomeForDateRange(
                userId,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 1)
        );

        assertThat(total).isEqualByComparingTo("0");
    }
}
