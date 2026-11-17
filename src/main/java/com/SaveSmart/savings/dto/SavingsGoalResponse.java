
package com.SaveSmart.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private String description;
    private LocalDateTime createdAt;
    private double progressPercentage;

    public SavingsGoalResponse(
            Long id,
            String goalName,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate targetDate,
            String description,
            LocalDateTime createdAt,
            double progressPercentage) {

        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.description = description;
        this.createdAt = createdAt;
        this.progressPercentage = progressPercentage;
    }

    public Long getId() {
        return id;
    }

    public String getGoalName() {
        return goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }
}
