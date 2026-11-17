
package com.SaveSmart.savings.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.savings.dto.SavingsTransactionRequest;
import com.SaveSmart.savings.dto.SavingsTransactionResponse;
import com.SaveSmart.savings.service.SavingsTransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/savings/accounts/{accountId}/transactions")
public class SavingsTransactionController {

    private final SavingsTransactionService savingsTransactionService;

    public SavingsTransactionController(
            SavingsTransactionService savingsTransactionService) {

        this.savingsTransactionService = savingsTransactionService;
    }

    // Deposit money
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsTransactionResponse deposit(
            Authentication authentication,
            @PathVariable Long accountId,
            @Valid @RequestBody SavingsTransactionRequest request) {

        String email = authentication.getName();

        return savingsTransactionService.deposit(
                email, accountId, request
        );
    }

    // Get transaction history
    @GetMapping
    public List<SavingsTransactionResponse> getTransactions(
            Authentication authentication,
            @PathVariable Long accountId) {

        String email = authentication.getName();

        return savingsTransactionService.getTransactions(
                email, accountId
        );
    }
}
