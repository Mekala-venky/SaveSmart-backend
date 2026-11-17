package com.SaveSmart.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentTransactionResponse {

    private Long id;
    private String accountType;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    public RecentTransactionResponse(
            Long id,
            String accountType,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDateTime transactionDate) {

        this.id = id;
        this.accountType = accountType;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public String getAccountType() {
        return accountType;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
