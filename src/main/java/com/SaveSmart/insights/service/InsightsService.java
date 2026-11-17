package com.SaveSmart.insights.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.dashboard.dto.CategoryExpenseResponse;
import com.SaveSmart.dashboard.dto.DashboardResponse;
import com.SaveSmart.dashboard.service.DashboardService;
import com.SaveSmart.insights.dto.InsightResponse;
import com.SaveSmart.insights.dto.InsightsResponse;
import com.SaveSmart.savings.entity.SavingsGoal;
import com.SaveSmart.savings.repository.SavingsGoalRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class InsightsService {

    private static final String SUCCESS = "SUCCESS";
    private static final String INFO = "INFO";
    private static final String WARNING = "WARNING";

    private static final BigDecimal RECOMMENDED_SAVINGS_RATE =
            BigDecimal.valueOf(20);

    private final DashboardService dashboardService;
    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    public InsightsService(
            DashboardService dashboardService,
            SavingsGoalRepository savingsGoalRepository,
            UserRepository userRepository) {

        this.dashboardService = dashboardService;
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public InsightsResponse getInsights(String email) {

        DashboardResponse dashboard = dashboardService.getDashboard(
                email, null, null
        );

        List<InsightResponse> insights = new ArrayList<>();

        if (!dashboard.isSalaryConfigured()) {

            insights.add(new InsightResponse(
                    WARNING,
                    "Add your monthly salary",
                    "SaveSmart needs your salary to calculate your savings "
                            + "rate, suggest an allocation, and score your "
                            + "financial health."
            ));

            insights.add(new InsightResponse(
                    INFO,
                    "Start tracking",
                    "Add your salary and this month's expenses to unlock "
                            + "personalised savings insights."
            ));

            return new InsightsResponse(
                    0,
                    "Not enough data",
                    0.0,
                    insights
            );
        }

        User user = getUserByEmail(email);

        List<SavingsGoal> goals =
                savingsGoalRepository.findByUserId(user.getId());

        BigDecimal salary = dashboard.getMonthlySalary();
        BigDecimal expenses = dashboard.getTotalExpenses();
        double savingsRate = dashboard.getSavingsPercentage();

        addSavingsRateInsight(insights, savingsRate, salary, expenses);
        addAllocationInsight(insights, salary);
        addEmergencyFundInsight(
                insights,
                dashboard.getEmergencyFundBalance(),
                expenses
        );
        addCategoryInsight(insights, dashboard.getExpensesByCategory());
        addGoalInsights(insights, goals);

        int healthScore = calculateHealthScore(
                savingsRate,
                dashboard,
                goals
        );

        return new InsightsResponse(
                healthScore,
                ratingFor(healthScore),
                savingsRate,
                insights
        );
    }

    private void addSavingsRateInsight(
            List<InsightResponse> insights,
            double savingsRate,
            BigDecimal salary,
            BigDecimal expenses) {

        if (savingsRate >= 20) {

            insights.add(new InsightResponse(
                    SUCCESS,
                    "Strong savings rate",
                    "You are keeping " + formatPercentage(savingsRate)
                            + " of your income after expenses. Transfer part "
                            + "of it into your savings accounts to make it "
                            + "official."
            ));

            return;
        }

        if (savingsRate >= 10) {

            insights.add(new InsightResponse(
                    INFO,
                    "Room to grow your savings",
                    "Your savings rate is " + formatPercentage(savingsRate)
                            + ". A common target is at least 20% of your "
                            + "monthly income."
            ));

            return;
        }

        if (savingsRate >= 0) {

            insights.add(new InsightResponse(
                    WARNING,
                    "Low savings rate",
                    "Only " + formatPercentage(savingsRate)
                            + " of your income is left after expenses. "
                            + "Review your largest categories for easy cuts."
            ));

            return;
        }

        insights.add(new InsightResponse(
                WARNING,
                "Spending exceeds income",
                "Your expenses are " + formatMoney(expenses.subtract(salary))
                        + " more than your salary this month. Consider "
                        + "pausing non-essential spending."
        ));
    }

    private void addAllocationInsight(
            List<InsightResponse> insights,
            BigDecimal salary) {

        BigDecimal recommendedSavings = salary
                .multiply(RECOMMENDED_SAVINGS_RATE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal futureSavings = recommendedSavings
                .multiply(BigDecimal.valueOf(60))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal emergencyFund = recommendedSavings.subtract(futureSavings);

        insights.add(new InsightResponse(
                INFO,
                "Suggested monthly allocation",
                "Try saving " + formatMoney(recommendedSavings)
                        + " (20% of your salary): about "
                        + formatMoney(futureSavings) + " toward future "
                        + "savings and " + formatMoney(emergencyFund)
                        + " toward your emergency fund."
        ));
    }

    private void addEmergencyFundInsight(
            List<InsightResponse> insights,
            BigDecimal emergencyFundBalance,
            BigDecimal monthlyExpenses) {

        if (monthlyExpenses.compareTo(BigDecimal.ZERO) <= 0) {

            insights.add(new InsightResponse(
                    INFO,
                    "Measure your emergency fund",
                    "Add this month's expenses so SaveSmart can tell you how "
                            + "many months your emergency fund would cover."
            ));

            return;
        }

        double monthsCovered = emergencyFundBalance
                .divide(monthlyExpenses, 2, RoundingMode.HALF_UP)
                .doubleValue();

        BigDecimal sixMonthTarget = monthlyExpenses
                .multiply(BigDecimal.valueOf(6));

        if (monthsCovered >= 6) {

            insights.add(new InsightResponse(
                    SUCCESS,
                    "Emergency fund is well funded",
                    "Your emergency fund covers about "
                            + formatMonths(monthsCovered)
                            + " of expenses, above the recommended 3–6 "
                            + "months."
            ));

            return;
        }

        if (monthsCovered >= 3) {

            insights.add(new InsightResponse(
                    INFO,
                    "Emergency fund is on track",
                    "Your emergency fund covers about "
                            + formatMonths(monthsCovered)
                            + " of expenses, within the recommended 3–6 "
                            + "month range. A full target is "
                            + formatMoney(sixMonthTarget) + "."
            ));

            return;
        }

        insights.add(new InsightResponse(
                WARNING,
                "Build up your emergency fund",
                "Your emergency fund covers about "
                        + formatMonths(monthsCovered)
                        + " of expenses. Aim for at least 3 months, or "
                        + formatMoney(sixMonthTarget) + " for 6 months."
        ));
    }

    private void addCategoryInsight(
            List<InsightResponse> insights,
            List<CategoryExpenseResponse> categories) {

        if (categories.isEmpty()) {

            insights.add(new InsightResponse(
                    INFO,
                    "No expenses recorded",
                    "Add your expenses for this month to see where your "
                            + "money is going."
            ));

            return;
        }

        CategoryExpenseResponse topCategory = categories.get(0);

        if (topCategory.getPercentage() >= 35) {

            insights.add(new InsightResponse(
                    WARNING,
                    "Watch your " + topCategory.getCategory() + " spending",
                    topCategory.getCategory() + " accounts for "
                            + formatPercentage(topCategory.getPercentage())
                            + " of this month's expenses ("
                            + formatMoney(topCategory.getAmount())
                            + "). Setting a category limit could free up "
                            + "more savings."
            ));

            return;
        }

        if (topCategory.getPercentage() >= 25) {

            insights.add(new InsightResponse(
                    INFO,
                    "Largest expense category",
                    topCategory.getCategory() + " is your biggest category at "
                            + formatPercentage(topCategory.getPercentage())
                            + " of this month's expenses ("
                            + formatMoney(topCategory.getAmount()) + ")."
            ));
        }
    }

    private void addGoalInsights(
            List<InsightResponse> insights,
            List<SavingsGoal> goals) {

        if (goals.isEmpty()) {

            insights.add(new InsightResponse(
                    INFO,
                    "Create your first savings goal",
                    "Goals like an emergency fund or a holiday make saving "
                            + "easier to track. Add a target amount and date."
            ));

            return;
        }

        long achievedGoals = goals.stream()
                .filter(goal -> goal.getCurrentAmount()
                        .compareTo(goal.getTargetAmount()) >= 0)
                .count();

        insights.add(new InsightResponse(
                achievedGoals == goals.size() ? SUCCESS : INFO,
                "Goal progress",
                achievedGoals + " of " + goals.size()
                        + " savings goals achieved so far."
        ));

        LocalDate today = LocalDate.now();
        int dueSoonCount = 0;

        for (SavingsGoal goal : goals) {

            boolean achieved = goal.getCurrentAmount()
                    .compareTo(goal.getTargetAmount()) >= 0;

            if (achieved) {
                continue;
            }

            long daysLeft = ChronoUnit.DAYS.between(
                    today, goal.getTargetDate()
            );

            if (daysLeft < 0) {

                insights.add(new InsightResponse(
                        WARNING,
                        "Goal overdue: " + goal.getGoalName(),
                        "This goal passed its target date "
                                + Math.abs(daysLeft) + " day(s) ago. "
                                + formatMoney(
                                        goal.getTargetAmount()
                                                .subtract(goal.getCurrentAmount())
                                )
                                + " is still needed."
                ));

                continue;
            }

            if (daysLeft <= 30 && dueSoonCount < 2) {

                dueSoonCount++;

                insights.add(new InsightResponse(
                        INFO,
                        "Goal due soon: " + goal.getGoalName(),
                        "Only " + daysLeft + " day(s) left to save "
                                + formatMoney(
                                        goal.getTargetAmount()
                                                .subtract(goal.getCurrentAmount())
                                )
                                + "."
                ));
            }
        }
    }

    private int calculateHealthScore(
            double savingsRate,
            DashboardResponse dashboard,
            List<SavingsGoal> goals) {

        int score = 0;

        if (savingsRate >= 20) {
            score += 40;
        } else if (savingsRate >= 10) {
            score += 30;
        } else if (savingsRate >= 0) {
            score += 15;
        }

        BigDecimal monthlyExpenses = dashboard.getTotalExpenses();
        BigDecimal emergencyBalance = dashboard.getEmergencyFundBalance();

        double monthsCovered = monthlyExpenses.compareTo(BigDecimal.ZERO) > 0
                ? emergencyBalance
                        .divide(
                                monthlyExpenses,
                                2,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue()
                : (emergencyBalance.compareTo(BigDecimal.ZERO) > 0 ? 6 : 0);

        if (monthsCovered >= 6) {
            score += 30;
        } else if (monthsCovered >= 3) {
            score += 25;
        } else if (monthsCovered >= 1) {
            score += 15;
        } else if (monthsCovered > 0) {
            score += 5;
        }

        if (!goals.isEmpty()) {

            double averageProgress = goals.stream()
                    .mapToDouble(goal -> Math.min(
                            100.0,
                            goal.getCurrentAmount()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            goal.getTargetAmount(),
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                                    .doubleValue()
                    ))
                    .average()
                    .orElse(0.0);

            score += (int) Math.round(averageProgress * 0.2);
        }

        List<CategoryExpenseResponse> categories =
                dashboard.getExpensesByCategory();

        if (categories.isEmpty()) {
            score += 5;
        } else {
            double topShare = categories.get(0).getPercentage();

            if (topShare < 30) {
                score += 10;
            } else if (topShare < 50) {
                score += 5;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private String ratingFor(int healthScore) {

        if (healthScore >= 80) {
            return "Excellent";
        }

        if (healthScore >= 60) {
            return "Good";
        }

        if (healthScore >= 40) {
            return "Fair";
        }

        return "Needs attention";
    }

    private String formatMoney(BigDecimal amount) {

        return "₹" + amount
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String formatPercentage(double percentage) {

        return BigDecimal.valueOf(percentage)
                .setScale(1, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private String formatMonths(double months) {

        return BigDecimal.valueOf(months)
                .setScale(1, RoundingMode.HALF_UP)
                .toPlainString() + " month(s)";
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }
}
