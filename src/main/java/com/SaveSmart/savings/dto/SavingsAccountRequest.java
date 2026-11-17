
package com.SaveSmart.savings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SavingsAccountRequest {

    @NotBlank(message = "Account type is required")
    @Pattern(
        regexp = "FUTURE_SAVINGS|EMERGENCY_FUND",
        message = "Invalid account type"
    )
    private String accountType;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
