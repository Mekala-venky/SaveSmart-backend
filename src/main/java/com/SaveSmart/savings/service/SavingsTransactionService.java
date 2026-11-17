
package com.SaveSmart.savings.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.service.BankAccountService;
import com.SaveSmart.savings.dto.SavingsTransactionRequest;
import com.SaveSmart.savings.dto.SavingsTransactionResponse;
import com.SaveSmart.savings.entity.SavingsAccount;
import com.SaveSmart.savings.entity.SavingsTransaction;
import com.SaveSmart.savings.repository.SavingsAccountRepository;
import com.SaveSmart.savings.repository.SavingsTransactionRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class SavingsTransactionService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;

    public SavingsTransactionService(
            SavingsAccountRepository savingsAccountRepository,
            SavingsTransactionRepository savingsTransactionRepository,
            UserRepository userRepository,
            BankAccountService bankAccountService) {

        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.userRepository = userRepository;
        this.bankAccountService = bankAccountService;
    }

    // Deposit money
    @Transactional
    public SavingsTransactionResponse deposit(
            String email,
            Long accountId,
            SavingsTransactionRequest request) {

        SavingsAccount account =
                getAccountForUser(email, accountId);

        // Money is saved from the simulated bank account, so debit it first.
        bankAccountService.debitForSavingsTransfer(
                email,
                request.getAmount(),
                bankDescription(
                        "To",
                        account.getAccountType(),
                        request.getDescription()
                )
        );

        // Add deposit amount to balance
        account.setBalance(
                account.getBalance().add(request.getAmount())
        );

        savingsAccountRepository.save(account);

        // Create transaction record
        return saveTransaction(
                account,
                "DEPOSIT",
                request
        );
    }

    // Get transaction history
    @Transactional(readOnly = true)
    public List<SavingsTransactionResponse> getTransactions(
            String email,
            Long accountId) {

        SavingsAccount account =
                getAccountForUser(email, accountId);

        return savingsTransactionRepository
                .findBySavingsAccountIdOrderByTransactionDateDesc(
                        account.getId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Verify account ownership
    private SavingsAccount getAccountForUser(
            String email,
            Long accountId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        SavingsAccount account =
                savingsAccountRepository.findById(accountId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Savings account not found"
                        ));

        if (!account.getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Savings account not found"
            );
        }

        return account;
    }

    // Build a short, readable description for the bank statement.
    // Shared with the withdrawal approval flow.
    static String bankDescription(
            String direction,
            String accountType,
            String description) {

        String label = "FUTURE_SAVINGS".equals(accountType)
                ? "future savings"
                : "EMERGENCY_FUND".equals(accountType)
                        ? "emergency fund"
                        : accountType;

        String prefix = direction + " " + label;

        if (description == null || description.isBlank()) {
            return prefix;
        }

        String combined = prefix + " · " + description;

        return combined.length() <= 255
                ? combined
                : combined.substring(0, 255);
    }

    // Save transaction record
    private SavingsTransactionResponse saveTransaction(
            SavingsAccount account,
            String transactionType,
            SavingsTransactionRequest request) {

        SavingsTransaction transaction =
                new SavingsTransaction();

        transaction.setTransactionType(transactionType);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setSavingsAccount(account);

        SavingsTransaction savedTransaction =
                savingsTransactionRepository.save(transaction);

        return convertToResponse(savedTransaction);
    }

    // Convert Entity into Response DTO
    private SavingsTransactionResponse convertToResponse(
            SavingsTransaction transaction) {

        return new SavingsTransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getSavingsAccount().getId()
        );
    }
}
