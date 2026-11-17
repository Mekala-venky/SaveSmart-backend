package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlyAnalysisResponse {

    private int month;
    private String monthName;
    private int year;

    private BigDecimal monthlySalary;
    private BigDecimal totalExpenses;
    private BigDecimal availableAmount;

    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netSaved;
    private double savingsPercentage;

    private List<CategoryExpenseResponse> expensesByCategory;

    public MonthlyAnalysisResponse(
            int month,
            String monthName,
            int year,
            BigDecimal monthlySalary,
            BigDecimal totalExpenses,
            BigDecimal availableAmount,
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            BigDecimal netSaved,
            double savingsPercentage,
            List<CategoryExpenseResponse> expensesByCategory) {

        this.month = month;
        this.monthName = monthName;
        this.year = year;
        this.monthlySalary = monthlySalary;
        this.totalExpenses = totalExpenses;
        this.availableAmount = availableAmount;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.netSaved = netSaved;
        this.savingsPercentage = savingsPercentage;
        this.expensesByCategory = expensesByCategory;
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

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public BigDecimal getNetSaved() {
        return netSaved;
    }

    public double getSavingsPercentage() {
        return savingsPercentage;
    }

    public List<CategoryExpenseResponse> getExpensesByCategory() {
        return expensesByCategory;
    }
}
