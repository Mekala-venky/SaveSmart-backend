package com.SaveSmart.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public class BankAccountRequest {

    @NotNull(message = "Opening balance is required")
    @DecimalMin(
        value = "0.00",
        message = "Opening balance cannot be negative"
    )
    @Digits(
        integer = 10,
        fraction = 2,
        message = "Opening balance must have at most 2 decimal places"
    )
    private BigDecimal openingBalance;

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }
}
