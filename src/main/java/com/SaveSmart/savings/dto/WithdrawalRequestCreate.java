package com.SaveSmart.savings.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * The user must explain why the money is needed. The reviewer reads this
 * description before approving or rejecting the request.
 */
public class WithdrawalRequestCreate {

    @NotNull(message = "Savings account is required")
    private Long savingsAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(
        value = "0.01",
        message = "Amount must be greater than zero"
    )
    @Digits(
        integer = 10,
        fraction = 2,
        message = "Amount must have at most 2 decimal places"
    )
    private BigDecimal amount;

    @NotBlank(
        message = "Explain why you need this money"
    )
    @Size(
        min = 10,
        max = 255,
        message = "Give a clear reason of at least 10 characters"
    )
    private String description;

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public void setSavingsAccountId(Long savingsAccountId) {
        this.savingsAccountId = savingsAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
