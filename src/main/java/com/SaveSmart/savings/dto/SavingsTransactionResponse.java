
package com.SaveSmart.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SavingsTransactionResponse {

    private Long id;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private Long savingsAccountId;

    public SavingsTransactionResponse(
            Long id,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDateTime transactionDate,
            Long savingsAccountId) {

        this.id = id;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.savingsAccountId = savingsAccountId;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }
}
