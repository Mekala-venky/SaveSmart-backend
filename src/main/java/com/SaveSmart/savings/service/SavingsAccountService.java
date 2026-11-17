
package com.SaveSmart.savings.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.savings.dto.SavingsAccountRequest;
import com.SaveSmart.savings.dto.SavingsAccountResponse;
import com.SaveSmart.savings.entity.SavingsAccount;
import com.SaveSmart.savings.repository.SavingsAccountRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final UserRepository userRepository;

    public SavingsAccountService(
            SavingsAccountRepository savingsAccountRepository,
            UserRepository userRepository) {

        this.savingsAccountRepository = savingsAccountRepository;
        this.userRepository = userRepository;
    }

    // Create a savings account
    public SavingsAccountResponse createAccount(
            String email,
            SavingsAccountRequest request) {

        User user = getUserByEmail(email);

        String accountType = request.getAccountType();

        // Check whether account already exists
        if (savingsAccountRepository.existsByUserIdAndAccountType(
                user.getId(), accountType)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Savings account already exists"
            );
        }

        // Create new account
        SavingsAccount account = new SavingsAccount();

        account.setAccountType(accountType);
        account.setBalance(java.math.BigDecimal.ZERO);
        account.setUser(user);

        SavingsAccount savedAccount =
                savingsAccountRepository.save(account);

        return convertToResponse(savedAccount);
    }

    // Get all savings accounts of logged-in user
    public List<SavingsAccountResponse> getAccounts(String email) {

        User user = getUserByEmail(email);

        return savingsAccountRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Find user using email
    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Convert Entity into Response DTO
    private SavingsAccountResponse convertToResponse(
            SavingsAccount account) {

        return new SavingsAccountResponse(
                account.getId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
