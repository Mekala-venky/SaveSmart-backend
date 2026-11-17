
package com.SaveSmart.savings.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.savings.dto.SavingsAccountRequest;
import com.SaveSmart.savings.dto.SavingsAccountResponse;
import com.SaveSmart.savings.service.SavingsAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/savings/accounts")
public class SavingsAccountController {

    private final SavingsAccountService savingsAccountService;

    public SavingsAccountController(
            SavingsAccountService savingsAccountService) {

        this.savingsAccountService = savingsAccountService;
    }

    // Create savings account
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsAccountResponse createAccount(
            Authentication authentication,
            @Valid @RequestBody SavingsAccountRequest request) {

        String email = authentication.getName();

        return savingsAccountService.createAccount(email, request);
    }

    // Get all savings accounts
    @GetMapping
    public List<SavingsAccountResponse> getAccounts(
            Authentication authentication) {

        String email = authentication.getName();

        return savingsAccountService.getAccounts(email);
    }
}
