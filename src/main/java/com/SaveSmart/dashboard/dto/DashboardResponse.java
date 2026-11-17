package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private int month;
    private String monthName;
    private int year;

    private BigDecimal monthlySalary;
    private BigDecimal totalExpenses;
    private BigDecimal availableAmount;
    private double savingsPercentage;

    private BigDecimal totalSavingsBalance;
    private BigDecimal futureSavingsBalance;
    private BigDecimal emergencyFundBalance;
    private BigDecimal bankBalance;
    private BigDecimal netSavedThisMonth;

    private int totalGoals;
    private int achievedGoals;

    private boolean salaryConfigured;

    private List<CategoryExpenseResponse> expensesByCategory;
    private List<RecentTransactionResponse> recentTransactions;

    public DashboardResponse(
            int month,
            String monthName,
            int year,
            BigDecimal monthlySalary,
            BigDecimal totalExpenses,
            BigDecimal availableAmount,
            double savingsPercentage,
            BigDecimal totalSavingsBalance,
            BigDecimal futureSavingsBalance,
            BigDecimal emergencyFundBalance,
            BigDecimal bankBalance,
            BigDecimal netSavedThisMonth,
            int totalGoals,
            int achievedGoals,
            boolean salaryConfigured,
            List<CategoryExpenseResponse> expensesByCategory,
            List<RecentTransactionResponse> recentTransactions) {

        this.month = month;
        this.monthName = monthName;
        this.year = year;
        this.monthlySalary = monthlySalary;
        this.totalExpenses = totalExpenses;
        this.availableAmount = availableAmount;
        this.savingsPercentage = savingsPercentage;
        this.totalSavingsBalance = totalSavingsBalance;
        this.futureSavingsBalance = futureSavingsBalance;
        this.emergencyFundBalance = emergencyFundBalance;
        this.bankBalance = bankBalance;
        this.netSavedThisMonth = netSavedThisMonth;
        this.totalGoals = totalGoals;
        this.achievedGoals = achievedGoals;
        this.salaryConfigured = salaryConfigured;
        this.expensesByCategory = expensesByCategory;
        this.recentTransactions = recentTransactions;
    }

    public int getMonth() {
        return month;
    }

    public String getMonthName() {
        return monthName;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public double getSavingsPercentage() {
        return savingsPercentage;
    }

    public BigDecimal getTotalSavingsBalance() {
        return totalSavingsBalance;
    }

    public BigDecimal getFutureSavingsBalance() {
        return futureSavingsBalance;
    }

    public BigDecimal getEmergencyFundBalance() {
        return emergencyFundBalance;
    }

    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    public BigDecimal getNetSavedThisMonth() {
        return netSavedThisMonth;
    }

    public int getTotalGoals() {
        return totalGoals;
    }

    public int getAchievedGoals() {
        return achievedGoals;
    }

    public boolean isSalaryConfigured() {
        return salaryConfigured;
    }

    public List<CategoryExpenseResponse> getExpensesByCategory() {
        return expensesByCategory;
    }

    public List<RecentTransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }
}
