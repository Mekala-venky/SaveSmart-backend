
package com.SaveSmart.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;

    public ExpenseResponse(
            Long id,
            BigDecimal amount,
            String category,
            String description,
            LocalDate expenseDate,
            LocalDateTime createdAt) {

        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
