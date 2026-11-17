package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;

public class MonthlyBreakdownResponse {

    private int month;
    private String monthName;
    private BigDecimal totalExpenses;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netSaved;

    public MonthlyBreakdownResponse(
            int month,
            String monthName,
            BigDecimal totalExpenses,
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            BigDecimal netSaved) {

        this.month = month;
        this.monthName = monthName;
        this.totalExpenses = totalExpenses;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.netSaved = netSaved;
    }

    public int getMonth() {
        return month;
    }

    public String getMonthName() {
        return monthName;
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
}
