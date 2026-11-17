package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;

public class CategoryExpenseResponse {

    private String category;
    private BigDecimal amount;
    private double percentage;

    public CategoryExpenseResponse(
            String category,
            BigDecimal amount,
            double percentage) {

        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getPercentage() {
        return percentage;
    }
}
