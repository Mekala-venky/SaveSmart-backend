package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class YearlyAnalysisResponse {

    private int year;

    private BigDecimal monthlySalary;
    private BigDecimal totalExpenses;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netSaved;
    private double savingsPercentage;

    private List<MonthlyBreakdownResponse> months;

    public YearlyAnalysisResponse(
            int year,
            BigDecimal monthlySalary,
            BigDecimal totalExpenses,
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            BigDecimal netSaved,
            double savingsPercentage,
            List<MonthlyBreakdownResponse> months) {

        this.year = year;
        this.monthlySalary = monthlySalary;
        this.totalExpenses = totalExpenses;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.netSaved = netSaved;
        this.savingsPercentage = savingsPercentage;
        this.months = months;
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

    public List<MonthlyBreakdownResponse> getMonths() {
        return months;
    }
}
