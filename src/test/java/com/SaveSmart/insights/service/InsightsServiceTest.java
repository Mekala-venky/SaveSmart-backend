package com.SaveSmart.insights.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.SaveSmart.dashboard.dto.CategoryExpenseResponse;
import com.SaveSmart.dashboard.dto.DashboardResponse;
import com.SaveSmart.dashboard.service.DashboardService;
import com.SaveSmart.insights.dto.InsightResponse;
import com.SaveSmart.insights.dto.InsightsResponse;
import com.SaveSmart.savings.entity.SavingsGoal;
import com.SaveSmart.savings.repository.SavingsGoalRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    private static final String EMAIL = "asha@example.com";

    @Mock
    private DashboardService dashboardService;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private UserRepository userRepository;

    private InsightsService service;

    @BeforeEach
    void setUp() {
        service = new InsightsService(
                dashboardService, savingsGoalRepository, userRepository
        );
    }

    @Test
    void missingSalaryReturnsNotEnoughData() {
        when(dashboardService.getDashboard(EMAIL, null, null))
                .thenReturn(dashboard(
                        false, "0", "0", 0.0, "0", List.of()
                ));

        InsightsResponse response = service.getInsights(EMAIL);

        assertThat(response.getHealthScore()).isZero();
        assertThat(response.getRating()).isEqualTo("Not enough data");
        assertThat(response.getSavingsRate()).isZero();
        assertThat(response.getInsights())
                .extracting(InsightResponse::getTitle)
                .containsExactly(
                        "Add your monthly salary",
                        "Start tracking"
                );
        assertThat(response.getInsights().get(0).getSeverity())
                .isEqualTo("WARNING");
    }

    @Test
    void healthyFinancesScoreExcellent() {
        stubUser();

        when(dashboardService.getDashboard(EMAIL, null, null))
                .thenReturn(dashboard(
                        true,
                        "50000",
                        "20000",
                        60.0,
                        "120000",
                        List.of(category("Food", "4000", 20.0))
                ));

        when(savingsGoalRepository.findByUserId(1L))
                .thenReturn(List.of(goal("Holiday", "10000", "5000", 60)));

        InsightsResponse response = service.getInsights(EMAIL);

        assertThat(response.getHealthScore()).isEqualTo(90);
        assertThat(response.getRating()).isEqualTo("Excellent");
        assertThat(response.getSavingsRate()).isEqualTo(60.0);
        assertThat(response.getInsights())
                .extracting(InsightResponse::getTitle)
                .contains(
                        "Strong savings rate",
                        "Suggested monthly allocation",
                        "Emergency fund is well funded",
                        "Goal progress"
                );
    }

    @Test
    void overspendingProducesWarnings() {
        stubUser();

        when(dashboardService.getDashboard(EMAIL, null, null))
                .thenReturn(dashboard(
                        true,
                        "30000",
                        "40000",
                        -33.33,
                        "0",
                        List.of(category("Rent", "20000", 50.0))
                ));

        when(savingsGoalRepository.findByUserId(1L))
                .thenReturn(List.of());

        InsightsResponse response = service.getInsights(EMAIL);

        assertThat(response.getHealthScore()).isZero();
        assertThat(response.getRating()).isEqualTo("Needs attention");
        assertThat(response.getInsights())
                .extracting(InsightResponse::getTitle)
                .contains(
                        "Spending exceeds income",
                        "Build up your emergency fund",
                        "Watch your Rent spending",
                        "Create your first savings goal"
                );

        assertThat(response.getInsights())
                .filteredOn(insight ->
                        "Spending exceeds income".equals(insight.getTitle()))
                .singleElement()
                .satisfies(insight -> assertThat(insight.getSeverity())
                        .isEqualTo("WARNING"));
    }

    @Test
    void overdueGoalIsFlagged() {
        stubUser();

        when(dashboardService.getDashboard(EMAIL, null, null))
                .thenReturn(dashboard(
                        true,
                        "50000",
                        "20000",
                        60.0,
                        "120000",
                        List.of(category("Food", "4000", 20.0))
                ));

        when(savingsGoalRepository.findByUserId(1L))
                .thenReturn(List.of(goal(
                        "Holiday",
                        "10000",
                        "0",
                        -2
                )));

        InsightsResponse response = service.getInsights(EMAIL);

        assertThat(response.getInsights())
                .extracting(InsightResponse::getTitle)
                .contains("Goal overdue: Holiday");
    }

    private void stubUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));
    }

    private DashboardResponse dashboard(
            boolean salaryConfigured,
            String salary,
            String expenses,
            double savingsPercentage,
            String emergencyBalance,
            List<CategoryExpenseResponse> categories) {

        BigDecimal monthlySalary = new BigDecimal(salary);
        BigDecimal totalExpenses = new BigDecimal(expenses);

        return new DashboardResponse(
                3,
                "March",
                2026,
                monthlySalary,
                totalExpenses,
                monthlySalary.subtract(totalExpenses),
                savingsPercentage,
                new BigDecimal(emergencyBalance),
                BigDecimal.ZERO,
                new BigDecimal(emergencyBalance),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                salaryConfigured,
                categories,
                List.of()
        );
    }

    private CategoryExpenseResponse category(
            String name,
            String amount,
            double percentage) {

        return new CategoryExpenseResponse(
                name, new BigDecimal(amount), percentage
        );
    }

    private SavingsGoal goal(
            String name,
            String target,
            String current,
            long daysFromToday) {

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(name);
        goal.setTargetAmount(new BigDecimal(target));
        goal.setCurrentAmount(new BigDecimal(current));
        goal.setTargetDate(LocalDate.now().plusDays(daysFromToday));

        return goal;
    }
}
