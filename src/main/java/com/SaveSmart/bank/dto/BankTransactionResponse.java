package com.SaveSmart.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankTransactionResponse {

    private Long id;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionDate;

    public BankTransactionResponse(
            Long id,
            String transactionType,
            BigDecimal amount,
            String description,
            BigDecimal balanceAfter,
            LocalDateTime transactionDate) {

        this.id = id;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
