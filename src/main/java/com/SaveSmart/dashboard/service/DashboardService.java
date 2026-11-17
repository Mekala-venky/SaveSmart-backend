package com.SaveSmart.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.entity.BankAccount;
import com.SaveSmart.bank.repository.BankAccountRepository;
import com.SaveSmart.dashboard.dto.CategoryExpenseResponse;
import com.SaveSmart.dashboard.dto.DashboardResponse;
import com.SaveSmart.dashboard.dto.MonthlyAnalysisResponse;
import com.SaveSmart.dashboard.dto.MonthlyBreakdownResponse;
import com.SaveSmart.dashboard.dto.RecentTransactionResponse;
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

@Service
public class DashboardService {

    private static final String FUTURE_SAVINGS = "FUTURE_SAVINGS";
    private static final String EMERGENCY_FUND = "EMERGENCY_FUND";
    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAWAL = "WITHDRAWAL";

    private final SalaryRepository salaryRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public DashboardService(
            SalaryRepository salaryRepository,
            ExpenseRepository expenseRepository,
            SavingsAccountRepository savingsAccountRepository,
            SavingsTransactionRepository savingsTransactionRepository,
            SavingsGoalRepository savingsGoalRepository,
            BankAccountRepository bankAccountRepository,
            UserRepository userRepository) {

        this.salaryRepository = salaryRepository;
        this.expenseRepository = expenseRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    // Dashboard summary for the selected month
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            String email,
            Integer month,
            Integer year) {

        YearMonth selectedMonth = resolveYearMonth(month, year);

        User user = getUserByEmail(email);

        Salary salary = salaryRepository.findByUserId(user.getId())
                .orElse(null);

        BigDecimal monthlySalary = salary != null
                ? salary.getMonthlySalary()
                : BigDecimal.ZERO;

        List<Expense> expenses =
                findExpensesForMonth(user.getId(), selectedMonth);

        List<SavingsTransaction> transactions =
                findTransactionsForMonth(user.getId(), selectedMonth);

        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal totalDeposits = sumByType(transactions, DEPOSIT);
        BigDecimal totalWithdrawals = sumByType(transactions, WITHDRAWAL);
        BigDecimal netSaved = totalDeposits.subtract(totalWithdrawals);

        List<SavingsAccount> accounts =
                savingsAccountRepository.findByUserId(user.getId());

        BigDecimal totalSavingsBalance = accounts.stream()
                .map(SavingsAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal futureSavingsBalance = sumAccountBalance(
                accounts, FUTURE_SAVINGS
        );

        BigDecimal emergencyFundBalance = sumAccountBalance(
                accounts, EMERGENCY_FUND
        );

        BigDecimal bankBalance = bankAccountRepository
                .findByUserId(user.getId())
                .map(BankAccount::getBalance)
                .orElse(BigDecimal.ZERO);

        List<SavingsGoal> goals =
                savingsGoalRepository.findByUserId(user.getId());

        int achievedGoals = (int) goals.stream()
                .filter(this::isGoalAchieved)
                .count();

        List<RecentTransactionResponse> recentTransactions =
                savingsTransactionRepository
                        .findTop5BySavingsAccountUserIdOrderByTransactionDateDesc(
                                user.getId()
                        )
                        .stream()
                        .map(this::convertToRecentTransaction)
                        .toList();

        return new DashboardResponse(
                selectedMonth.getMonthValue(),
                monthName(selectedMonth.getMonthValue()),
                selectedMonth.getYear(),
                monthlySalary,
                totalExpenses,
                monthlySalary.subtract(totalExpenses),
                savingsPercentage(monthlySalary, totalExpenses),
                totalSavingsBalance,
                futureSavingsBalance,
                emergencyFundBalance,
                bankBalance,
                netSaved,
                goals.size(),
                achievedGoals,
                salary != null,
                buildCategoryBreakdown(expenses),
                recentTransactions
        );
    }

    // Detailed analysis for a single month
    @Transactional(readOnly = true)
    public MonthlyAnalysisResponse getMonthlyAnalysis(
            String email,
            Integer month,
            Integer year) {

        YearMonth selectedMonth = resolveYearMonth(month, year);

        User user = getUserByEmail(email);

        Salary salary = salaryRepository.findByUserId(user.getId())
                .orElse(null);

        BigDecimal monthlySalary = salary != null
                ? salary.getMonthlySalary()
                : BigDecimal.ZERO;

        List<Expense> expenses =
                findExpensesForMonth(user.getId(), selectedMonth);

        List<SavingsTransaction> transactions =
                findTransactionsForMonth(user.getId(), selectedMonth);

        BigDecimal totalExpenses = sumExpenses(expenses);
        BigDecimal totalDeposits = sumByType(transactions, DEPOSIT);
        BigDecimal totalWithdrawals = sumByType(transactions, WITHDRAWAL);

        return new MonthlyAnalysisResponse(
                selectedMonth.getMonthValue(),
                monthName(selectedMonth.getMonthValue()),
                selectedMonth.getYear(),
                monthlySalary,
                totalExpenses,
                monthlySalary.subtract(totalExpenses),
                totalDeposits,
                totalWithdrawals,
                totalDeposits.subtract(totalWithdrawals),
                savingsPercentage(monthlySalary, totalExpenses),
                buildCategoryBreakdown(expenses)
        );
    }

    // Yearly analysis with a month-by-month breakdown
    @Transactional(readOnly = true)
    public YearlyAnalysisResponse getYearlyAnalysis(
            String email,
            Integer year) {

        int selectedYear = year != null
                ? validateYear(year)
                : YearMonth.now().getYear();

        User user = getUserByEmail(email);

        Salary salary = salaryRepository.findByUserId(user.getId())
                .orElse(null);

        BigDecimal monthlySalary = salary != null
                ? salary.getMonthlySalary()
                : BigDecimal.ZERO;

        LocalDate startDate = LocalDate.of(selectedYear, 1, 1);
        LocalDate endDate = LocalDate.of(selectedYear, 12, 31);

        List<Expense> expenses =
                expenseRepository.findByUserIdAndExpenseDateBetween(
                        user.getId(), startDate, endDate
                );

        List<SavingsTransaction> transactions =
                savingsTransactionRepository
                        .findBySavingsAccountUserIdAndTransactionDateBetween(
                                user.getId(),
                                startDate.atStartOfDay(),
                                endDate.atTime(LocalTime.MAX)
                        );

        Map<Integer, BigDecimal> expensesByMonth = new HashMap<>();
        Map<Integer, BigDecimal> depositsByMonth = new HashMap<>();
        Map<Integer, BigDecimal> withdrawalsByMonth = new HashMap<>();

        for (Expense expense : expenses) {
            expensesByMonth.merge(
                    expense.getExpenseDate().getMonthValue(),
                    expense.getAmount(),
                    BigDecimal::add
            );
        }

        for (SavingsTransaction transaction : transactions) {
            Map<Integer, BigDecimal> target =
                    DEPOSIT.equals(transaction.getTransactionType())
                            ? depositsByMonth
                            : withdrawalsByMonth;

            target.merge(
                    transaction.getTransactionDate().getMonthValue(),
                    transaction.getAmount(),
                    BigDecimal::add
            );
        }

        List<MonthlyBreakdownResponse> months = new ArrayList<>();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;

        for (int monthValue = 1; monthValue <= 12; monthValue++) {

            BigDecimal monthExpenses = expensesByMonth
                    .getOrDefault(monthValue, BigDecimal.ZERO);

            BigDecimal monthDeposits = depositsByMonth
                    .getOrDefault(monthValue, BigDecimal.ZERO);

            BigDecimal monthWithdrawals = withdrawalsByMonth
                    .getOrDefault(monthValue, BigDecimal.ZERO);

            totalExpenses = totalExpenses.add(monthExpenses);
            totalDeposits = totalDeposits.add(monthDeposits);
            totalWithdrawals = totalWithdrawals.add(monthWithdrawals);

            months.add(new MonthlyBreakdownResponse(
                    monthValue,
                    monthName(monthValue),
                    monthExpenses,
                    monthDeposits,
                    monthWithdrawals,
                    monthDeposits.subtract(monthWithdrawals)
            ));
        }

        BigDecimal yearlySalary = monthlySalary
                .multiply(BigDecimal.valueOf(12));

        return new YearlyAnalysisResponse(
                selectedYear,
                monthlySalary,
                totalExpenses,
                totalDeposits,
                totalWithdrawals,
                totalDeposits.subtract(totalWithdrawals),
                savingsPercentage(yearlySalary, totalExpenses),
                months
        );
    }

    private List<Expense> findExpensesForMonth(
            Long userId,
            YearMonth selectedMonth) {

        return expenseRepository.findByUserIdAndExpenseDateBetween(
                userId,
                selectedMonth.atDay(1),
                selectedMonth.atEndOfMonth()
        );
    }

    private List<SavingsTransaction> findTransactionsForMonth(
            Long userId,
            YearMonth selectedMonth) {

        return savingsTransactionRepository
                .findBySavingsAccountUserIdAndTransactionDateBetween(
                        userId,
                        selectedMonth.atDay(1).atStartOfDay(),
                        selectedMonth.atEndOfMonth().atTime(LocalTime.MAX)
                );
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByType(
            List<SavingsTransaction> transactions,
            String transactionType) {

        return transactions.stream()
                .filter(transaction -> transactionType.equals(
                        transaction.getTransactionType()
                ))
                .map(SavingsTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumAccountBalance(
            List<SavingsAccount> accounts,
            String accountType) {

        return accounts.stream()
                .filter(account -> accountType.equals(
                        account.getAccountType()
                ))
                .map(SavingsAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isGoalAchieved(SavingsGoal goal) {

        return goal.getCurrentAmount()
                .compareTo(goal.getTargetAmount()) >= 0;
    }

    private List<CategoryExpenseResponse> buildCategoryBreakdown(
            List<Expense> expenses) {

        Map<String, BigDecimal> totalsByCategory = new HashMap<>();

        for (Expense expense : expenses) {
            totalsByCategory.merge(
                    expense.getCategory(),
                    expense.getAmount(),
                    BigDecimal::add
            );
        }

        BigDecimal totalExpenses = sumExpenses(expenses);

        return totalsByCategory.entrySet()
                .stream()
                .sorted((first, second) -> second.getValue()
                        .compareTo(first.getValue()))
                .map(entry -> new CategoryExpenseResponse(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalExpenses)
                ))
                .toList();
    }

    private RecentTransactionResponse convertToRecentTransaction(
            SavingsTransaction transaction) {

        return new RecentTransactionResponse(
                transaction.getId(),
                transaction.getSavingsAccount().getAccountType(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate()
        );
    }

    private double savingsPercentage(
            BigDecimal salary,
            BigDecimal expenses) {

        if (salary == null
                || salary.compareTo(BigDecimal.ZERO) <= 0) {

            return 0.0;
        }

        return salary.subtract(expenses)
                .multiply(BigDecimal.valueOf(100))
                .divide(salary, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double percentage(
            BigDecimal part,
            BigDecimal whole) {

        if (whole == null
                || whole.compareTo(BigDecimal.ZERO) <= 0) {

            return 0.0;
        }

        return part.multiply(BigDecimal.valueOf(100))
                .divide(whole, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private YearMonth resolveYearMonth(
            Integer month,
            Integer year) {

        YearMonth currentMonth = YearMonth.now();

        int resolvedYear = year != null
                ? validateYear(year)
                : currentMonth.getYear();

        int resolvedMonth = month != null
                ? validateMonth(month)
                : currentMonth.getMonthValue();

        return YearMonth.of(resolvedYear, resolvedMonth);
    }

    private int validateMonth(int month) {

        if (month < 1 || month > 12) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Month must be between 1 and 12"
            );
        }

        return month;
    }

    private int validateYear(int year) {

        if (year < 1900 || year > 9999) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid year"
            );
        }

        return year;
    }

    private String monthName(int monthValue) {

        return Month.of(monthValue)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }
}
