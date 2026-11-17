package com.SaveSmart.bank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.bank.dto.BankAccountRequest;
import com.SaveSmart.bank.dto.BankAccountResponse;
import com.SaveSmart.bank.dto.BankTransactionRequest;
import com.SaveSmart.bank.dto.BankTransactionResponse;
import com.SaveSmart.bank.service.BankAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bank")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    // Read the simulated bank account
    @GetMapping("/account")
    public BankAccountResponse getAccount(Authentication authentication) {

        return bankAccountService.getAccount(authentication.getName());
    }

    // Create the simulated bank account
    @PostMapping("/account")
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountResponse createAccount(
            Authentication authentication,
            @Valid @RequestBody BankAccountRequest request) {

        return bankAccountService.createAccount(
                authentication.getName(),
                request
        );
    }

    // Add simulated money (for example, salary received)
    @PostMapping("/account/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public BankTransactionResponse deposit(
            Authentication authentication,
            @Valid @RequestBody BankTransactionRequest request) {

        return bankAccountService.deposit(
                authentication.getName(),
                request
        );
    }

    // Take simulated cash out of the bank account
    @PostMapping("/account/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public BankTransactionResponse withdraw(
            Authentication authentication,
            @Valid @RequestBody BankTransactionRequest request) {

        return bankAccountService.withdraw(
                authentication.getName(),
                request
        );
    }

    // Simulated bank statement
    @GetMapping("/transactions")
    public List<BankTransactionResponse> getTransactions(
            Authentication authentication) {

        return bankAccountService.getTransactions(
                authentication.getName()
        );
    }
}
