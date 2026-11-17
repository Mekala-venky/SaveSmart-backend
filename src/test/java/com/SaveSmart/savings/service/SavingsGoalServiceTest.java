package com.SaveSmart.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.savings.dto.SavingsGoalContributionRequest;
import com.SaveSmart.savings.dto.SavingsGoalRequest;
import com.SaveSmart.savings.dto.SavingsGoalResponse;
import com.SaveSmart.savings.entity.SavingsGoal;
import com.SaveSmart.savings.repository.SavingsGoalRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    private static final String EMAIL = "asha@example.com";

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private UserRepository userRepository;

    private SavingsGoalService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new SavingsGoalService(
                savingsGoalRepository, userRepository
        );

        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setFullName("Asha Rao");

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));
    }

    @Test
    void createGoalStartsAtZeroProgress() {
        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("  Emergency fund  ");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setTargetDate(LocalDate.now().plusDays(30));
        request.setDescription("Three months of expenses");

        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavingsGoalResponse response = service.createGoal(EMAIL, request);

        assertThat(response.getGoalName()).isEqualTo("Emergency fund");
        assertThat(response.getTargetAmount())
                .isEqualByComparingTo("10000.00");
        assertThat(response.getCurrentAmount())
                .isEqualByComparingTo("0.00");
        assertThat(response.getProgressPercentage()).isZero();
    }

    @Test
    void addMoneyUpdatesProgress() {
        SavingsGoal goal = goalWith(1000, 400);
        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SavingsGoalResponse response = service.addMoney(
                EMAIL, 10L, contribution("250.00")
        );

        assertThat(response.getCurrentAmount())
                .isEqualByComparingTo("650.00");
        assertThat(response.getProgressPercentage()).isEqualTo(65.0);
    }

    @Test
    void addMoneyRejectsContributionAboveRemainingAmount() {
        SavingsGoal goal = goalWith(1000, 800);
        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.addMoney(
                EMAIL, 10L, contribution("300.00")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(savingsGoalRepository, never()).save(any());
    }

    @Test
    void addMoneyRejectsAlreadyAchievedGoal() {
        SavingsGoal goal = goalWith(1000, 1000);
        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.addMoney(
                EMAIL, 10L, contribution("50.00")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateGoalRejectsTargetBelowSavedAmount() {
        SavingsGoal goal = goalWith(1000, 700);
        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));

        SavingsGoalRequest request = new SavingsGoalRequest();
        request.setGoalName("Emergency fund");
        request.setTargetAmount(new BigDecimal("500.00"));
        request.setTargetDate(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> service.updateGoal(
                EMAIL, 10L, request
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void progressIsCappedAtOneHundredPercent() {
        SavingsGoal goal = goalWith(1000, 1500);
        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));

        SavingsGoalResponse response = service.getGoal(EMAIL, 10L);

        assertThat(response.getProgressPercentage()).isEqualTo(100.0);
    }

    @Test
    void goalOwnedByAnotherUserIsNotFound() {
        SavingsGoal goal = goalWith(1000, 100);

        User otherUser = new User();
        otherUser.setId(2L);

        goal.setUser(otherUser);

        when(savingsGoalRepository.findById(10L))
                .thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.getGoal(EMAIL, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode()
                ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private SavingsGoal goalWith(
            long targetAmount,
            long currentAmount) {

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName("Emergency fund");
        goal.setTargetAmount(BigDecimal.valueOf(targetAmount));
        goal.setCurrentAmount(BigDecimal.valueOf(currentAmount));
        goal.setTargetDate(LocalDate.now().plusDays(30));
        goal.setUser(user);

        return goal;
    }

    private SavingsGoalContributionRequest contribution(String amount) {
        SavingsGoalContributionRequest request =
                new SavingsGoalContributionRequest();
        request.setAmount(new BigDecimal(amount));

        return request;
    }
}
