package com.SaveSmart.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.repository.BankAccountRepository;
import com.SaveSmart.dashboard.dto.CategoryExpenseResponse;
import com.SaveSmart.dashboard.dto.DashboardResponse;
import com.SaveSmart.dashboard.dto.MonthlyAnalysisResponse;
import com.SaveSmart.dashboard.dto.YearlyAnalysisResponse;
import com.SaveSmart.expense.entity.Expense;
import com.SaveSmart.expense.repository.ExpenseRepository;
import com.SaveSmart.salary.entity.Salary;
import com.SaveSmart.salary.repository.SalaryRepository;
import com.SaveSmart.savings.entity.SavingsAccount;
import com.SaveSmart.savings.entity.SavingsGoal;
import com.SaveSmart.savings.entity.SavingsTransaction;
import com.SaveSmart.savings.repository.SavingsAccountRepository;
import com.SaveSmart.savings.repository.SavingsGoalRepository;
import com.SaveSmart.savings.repository.SavingsTransactionRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final String EMAIL = "asha@example.com";
    private static final Long USER_ID = 7L;
    private static final int YEAR = 2026;
    private static final int MONTH = 3;

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private SavingsTransactionRepository savingsTransactionRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(
                salaryRepository,
                expenseRepository,
                savingsAccountRepository,
                savingsTransactionRepository,
                savingsGoalRepository,
                bankAccountRepository,
                userRepository
        );
    }

    @Test
    void dashboardSummarisesSalaryExpensesAndSavings() {
        stubUser();

        Salary salary = new Salary();
        salary.setMonthlySalary(new BigDecimal("50000.00"));

        when(salaryRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(salary));

        List<Expense> expenses = List.of(
                expense("Rent", "15000.00", LocalDate.of(YEAR, MONTH, 2)),
                expense("Food", "5000.00", LocalDate.of(YEAR, MONTH, 8)),
                expense("Food", "3000.00", LocalDate.of(YEAR, MONTH, 12))
        );

        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                eq(USER_ID),
                eq(LocalDate.of(YEAR, MONTH, 1)),
                eq(LocalDate.of(YEAR, MONTH, 31))
        )).thenReturn(expenses);

        List<SavingsTransaction> transactions = List.of(
                transaction("DEPOSIT", "10000.00"),
                transaction("WITHDRAWAL", "2000.00")
        );

        when(savingsTransactionRepository
                .findBySavingsAccountUserIdAndTransactionDateBetween(
                        eq(USER_ID),
                        eq(LocalDate.of(YEAR, MONTH, 1).atStartOfDay()),
                        eq(LocalDate.of(YEAR, MONTH, 31)
                                .atTime(LocalTime.MAX))
                ))
                .thenReturn(transactions);

        when(savingsAccountRepository.findByUserId(USER_ID))
                .thenReturn(List.of(
                        account("FUTURE_SAVINGS", "12000.00"),
                        account("EMERGENCY_FUND", "8000.00")
                ));

        when(savingsGoalRepository.findByUserId(USER_ID))
                .thenReturn(List.of(
                        goal("1000.00", "1000.00"),
                        goal("5000.00", "2500.00")
                ));

        when(savingsTransactionRepository
                .findTop5BySavingsAccountUserIdOrderByTransactionDateDesc(
                        USER_ID
                ))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(
                EMAIL, MONTH, YEAR
        );

        assertThat(response.getMonth()).isEqualTo(MONTH);
        assertThat(response.getMonthName()).isEqualTo("March");
        assertThat(response.getYear()).isEqualTo(YEAR);
        assertThat(response.getMonthlySalary())
                .isEqualByComparingTo("50000.00");
        assertThat(response.getTotalExpenses())
                .isEqualByComparingTo("23000.00");
        assertThat(response.getAvailableAmount())
                .isEqualByComparingTo("27000.00");
        assertThat(response.getSavingsPercentage()).isEqualTo(54.0);
        assertThat(response.getTotalSavingsBalance())
                .isEqualByComparingTo("20000.00");
        assertThat(response.getFutureSavingsBalance())
                .isEqualByComparingTo("12000.00");
        assertThat(response.getEmergencyFundBalance())
                .isEqualByComparingTo("8000.00");
        assertThat(response.getNetSavedThisMonth())
                .isEqualByComparingTo("8000.00");
        assertThat(response.getTotalGoals()).isEqualTo(2);
        assertThat(response.getAchievedGoals()).isEqualTo(1);
        assertThat(response.isSalaryConfigured()).isTrue();

        assertThat(response.getExpensesByCategory())
                .extracting(CategoryExpenseResponse::getCategory)
                .containsExactly("Rent", "Food");

        CategoryExpenseResponse rent =
                response.getExpensesByCategory().get(0);
        assertThat(rent.getAmount()).isEqualByComparingTo("15000.00");
        assertThat(rent.getPercentage()).isEqualTo(65.22);
    }

    @Test
    void monthlyAnalysisSeparatesDepositsAndWithdrawals() {
        stubUser();

        Salary salary = new Salary();
        salary.setMonthlySalary(new BigDecimal("50000.00"));

        when(salaryRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(salary));

        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                any(), any(), any()
        )).thenReturn(List.of(
                expense("Food", "8000.00", LocalDate.of(YEAR, MONTH, 4))
        ));

        when(savingsTransactionRepository
                .findBySavingsAccountUserIdAndTransactionDateBetween(
                        any(), any(), any()
                ))
                .thenReturn(List.of(
                        transaction("DEPOSIT", "6000.00"),
                        transaction("WITHDRAWAL", "1500.00")
                ));

        MonthlyAnalysisResponse response = service.getMonthlyAnalysis(
                EMAIL, MONTH, YEAR
        );

        assertThat(response.getTotalExpenses())
                .isEqualByComparingTo("8000.00");
        assertThat(response.getAvailableAmount())
                .isEqualByComparingTo("42000.00");
        assertThat(response.getTotalDeposits())
                .isEqualByComparingTo("6000.00");
        assertThat(response.getTotalWithdrawals())
                .isEqualByComparingTo("1500.00");
        assertThat(response.getNetSaved())
                .isEqualByComparingTo("4500.00");
        assertThat(response.getSavingsPercentage()).isEqualTo(84.0);
    }

    @Test
    void yearlyAnalysisBuildsTwelveMonthBreakdown() {
        stubUser();

        Salary salary = new Salary();
        salary.setMonthlySalary(new BigDecimal("50000.00"));

        when(salaryRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(salary));

        LocalDate start = LocalDate.of(YEAR, 1, 1);
        LocalDate end = LocalDate.of(YEAR, 12, 31);

        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                USER_ID, start, end
        )).thenReturn(List.of(
                expense("Food", "5000.00", LocalDate.of(YEAR, 2, 10)),
                expense("Travel", "2500.00", LocalDate.of(YEAR, 12, 1))
        ));

        when(savingsTransactionRepository
                .findBySavingsAccountUserIdAndTransactionDateBetween(
                        USER_ID,
                        start.atStartOfDay(),
                        end.atTime(LocalTime.MAX)
                ))
                .thenReturn(List.of(
                        transaction(
                                "DEPOSIT", "10000.00",
                                LocalDateTime.of(YEAR, 3, 15, 10, 0)
                        ),
                        transaction(
                                "WITHDRAWAL", "3000.00",
                                LocalDateTime.of(YEAR, 3, 20, 10, 0)
                        ),
                        transaction(
                                "DEPOSIT", "5000.00",
                                LocalDateTime.of(YEAR, 12, 1, 10, 0)
                        )
                ));

        YearlyAnalysisResponse response = service.getYearlyAnalysis(
                EMAIL, YEAR
        );

        assertThat(response.getYear()).isEqualTo(YEAR);
        assertThat(response.getMonthlySalary())
                .isEqualByComparingTo("50000.00");
        assertThat(response.getMonths()).hasSize(12);
        assertThat(response.getTotalExpenses())
                .isEqualByComparingTo("7500.00");
        assertThat(response.getTotalDeposits())
                .isEqualByComparingTo("15000.00");
        assertThat(response.getTotalWithdrawals())
                .isEqualByComparingTo("3000.00");
        assertThat(response.getNetSaved())
                .isEqualByComparingTo("12000.00");
        assertThat(response.getSavingsPercentage()).isEqualTo(98.75);

        assertThat(response.getMonths().get(1).getMonthName())
                .isEqualTo("February");
        assertThat(response.getMonths().get(1).getTotalExpenses())
                .isEqualByComparingTo("5000.00");

        assertThat(response.getMonths().get(2).getTotalDeposits())
                .isEqualByComparingTo("10000.00");
        assertThat(response.getMonths().get(2).getTotalWithdrawals())
                .isEqualByComparingTo("3000.00");
        assertThat(response.getMonths().get(2).getNetSaved())
                .isEqualByComparingTo("7000.00");

        assertThat(response.getMonths().get(11).getNetSaved())
                .isEqualByComparingTo("5000.00");
    }

    @Test
    void invalidMonthIsRejected() {
        assertThatThrownBy(() -> service.getDashboard(EMAIL, 13, YEAR))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void salaryCanBeMissing() {
        stubUser();

        when(salaryRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());
        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                any(), any(), any()
        )).thenReturn(List.of());
        when(savingsTransactionRepository
                .findBySavingsAccountUserIdAndTransactionDateBetween(
                        any(), any(), any()
                ))
                .thenReturn(List.of());
        when(savingsAccountRepository.findByUserId(USER_ID))
                .thenReturn(List.of());
        when(savingsGoalRepository.findByUserId(USER_ID))
                .thenReturn(List.of());
        when(savingsTransactionRepository
                .findTop5BySavingsAccountUserIdOrderByTransactionDateDesc(
                        USER_ID
                ))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(
                EMAIL, MONTH, YEAR
        );

        assertThat(response.isSalaryConfigured()).isFalse();
        assertThat(response.getMonthlySalary())
                .isEqualByComparingTo("0");
        assertThat(response.getSavingsPercentage()).isZero();
        assertThat(response.getAvailableAmount())
                .isEqualByComparingTo("0");
    }

    private void stubUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));
    }

    private Expense expense(
            String category,
            String amount,
            LocalDate date) {

        Expense expense = new Expense();
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);

        return expense;
    }

    private SavingsTransaction transaction(
            String type,
            String amount) {

        return transaction(
                type, amount, LocalDateTime.of(YEAR, MONTH, 10, 12, 0)
        );
    }

    private SavingsTransaction transaction(
            String type,
            String amount,
            LocalDateTime date) {

        SavingsTransaction transaction = new SavingsTransaction();
        transaction.setTransactionType(type);
        transaction.setAmount(new BigDecimal(amount));
        setField(transaction, "transactionDate", date);

        return transaction;
    }

    private SavingsAccount account(String type, String balance) {
        SavingsAccount account = new SavingsAccount();
        account.setAccountType(type);
        account.setBalance(new BigDecimal(balance));

        return account;
    }

    private SavingsGoal goal(String target, String current) {
        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName("Goal");
        goal.setTargetAmount(new BigDecimal(target));
        goal.setCurrentAmount(new BigDecimal(current));
        goal.setTargetDate(LocalDate.of(YEAR, 12, 31));

        return goal;
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value) {

        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not set field " + fieldName, exception
            );
        }
    }
}
