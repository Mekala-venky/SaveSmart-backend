
package com.SaveSmart.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SavingsAccountResponse {

    private Long id;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SavingsAccountResponse(
            Long id,
            String accountType,
            BigDecimal balance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
