package com.SaveSmart.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankAccountResponse(
            Long id,
            String accountNumber,
            BigDecimal balance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
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
