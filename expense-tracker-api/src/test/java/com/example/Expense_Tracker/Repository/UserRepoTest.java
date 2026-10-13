package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("test")
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    private Users activeUser;
    private Users inactiveUser;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        activeUser = userRepo.save(
                Users.builder()
                        .full_name("Hari Prasad")
                        .email("hari@example.com")
                        .password_hash("hashed_password_1")
                        .currency("INR")
                        .isActive(true)
                        .build()
        );

        inactiveUser = userRepo.save(
                Users.builder()
                        .full_name("Inactive User")
                        .email("inactive@example.com")
                        .password_hash("hashed_password_2")
                        .currency("INR")
                        .isActive(false)
                        .build()
        );
    }

    @Test
    void saveUser_shouldGenerateIdAndApplyDefaults() {
        Users savedUser = userRepo.save(
                Users.builder()
                        .full_name("Test User")
                        .email("test@example.com")
                        .password_hash("hashed_password_3")
                        .build()
        );

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCurrency()).isEqualTo("INR");
        assertThat(savedUser.getIsActive()).isTrue();
    }

    @Test
    void findByEmail_shouldReturnMatchingUser() {
        Optional<Users> result =
                userRepo.findByEmail("hari@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFull_name())
                .isEqualTo("Hari Prasad");
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<Users> result =
                userRepo.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrueForExistingEmail() {
        assertThat(userRepo.existsByEmail("hari@example.com"))
                .isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseForUnknownEmail() {
        assertThat(userRepo.existsByEmail("missing@example.com"))
                .isFalse();
    }

    @Test
    void findByIsActiveTrue_shouldReturnOnlyActiveUsers() {
        List<Users> activeUsers = userRepo.findByIsActiveTrue();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail())
                .isEqualTo("hari@example.com");
        assertThat(activeUsers)
                .allMatch(user -> Boolean.TRUE.equals(user.getIsActive()));
    }

    @Test
    void findByIsActiveTrue_shouldExcludeInactiveUsers() {
        List<Users> activeUsers = userRepo.findByIsActiveTrue();

        assertThat(activeUsers)
                .noneMatch(user ->
                        user.getEmail().equals("inactive@example.com"));
    }

    @Test
    void saveUser_shouldPopulateCreationAndUpdateTimestamps() {
        Users savedUser = userRepo.saveAndFlush(
                Users.builder()
                        .full_name("Timestamp User")
                        .email("timestamp@example.com")
                        .password_hash("hashed_password_4")
                        .build()
        );

        assertThat(savedUser.getCreated_at()).isNotNull();
        assertThat(savedUser.getUpdated_at()).isNotNull();
    }
}