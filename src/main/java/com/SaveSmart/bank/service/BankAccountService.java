package com.SaveSmart.bank.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.dto.BankAccountRequest;
import com.SaveSmart.bank.dto.BankAccountResponse;
import com.SaveSmart.bank.dto.BankTransactionRequest;
import com.SaveSmart.bank.dto.BankTransactionResponse;
import com.SaveSmart.bank.entity.BankAccount;
import com.SaveSmart.bank.entity.BankTransaction;
import com.SaveSmart.bank.repository.BankAccountRepository;
import com.SaveSmart.bank.repository.BankTransactionRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class BankAccountService {

    public static final String OPENING_BALANCE = "OPENING_BALANCE";
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAWAL = "WITHDRAWAL";
    public static final String TRANSFER_TO_SAVINGS = "TRANSFER_TO_SAVINGS";
    public static final String TRANSFER_FROM_SAVINGS = "TRANSFER_FROM_SAVINGS";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final UserRepository userRepository;

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            BankTransactionRepository bankTransactionRepository,
            UserRepository userRepository) {

        this.bankAccountRepository = bankAccountRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.userRepository = userRepository;
    }

    // Read the user's simulated bank account
    @Transactional(readOnly = true)
    public BankAccountResponse getAccount(String email) {

        return convertToResponse(getAccountEntity(email));
    }

    // Create the user's simulated bank account with an opening balance
    @Transactional
    public BankAccountResponse createAccount(
            String email,
            BankAccountRequest request) {

        User user = getUserByEmail(email);

        if (bankAccountRepository.existsByUserId(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A simulated bank account already exists"
            );
        }

        BankAccount account = new BankAccount();

        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(request.getOpeningBalance());

        bankAccountRepository.save(account);

        if (request.getOpeningBalance().compareTo(BigDecimal.ZERO) > 0) {

            recordTransaction(
                    account,
                    OPENING_BALANCE,
                    request.getOpeningBalance(),
                    "Opening simulated balance"
            );
        }

        return convertToResponse(account);
    }

    // Add simulated money to the bank account
    @Transactional
    public BankTransactionResponse deposit(
            String email,
            BankTransactionRequest request) {

        BankAccount account = getAccountEntity(email);

        account.setBalance(
                account.getBalance().add(request.getAmount())
        );

        bankAccountRepository.save(account);

        return convertToResponse(recordTransaction(
                account,
                DEPOSIT,
                request.getAmount(),
                descriptionOrDefault(
                        request.getDescription(),
                        "Simulated funds added"
                )
        ));
    }

    // Take simulated cash out of the bank account
    @Transactional
    public BankTransactionResponse withdraw(
            String email,
            BankTransactionRequest request) {

        BankAccount account = getAccountEntity(email);

        requireBankBalance(account, request.getAmount());

        account.setBalance(
                account.getBalance().subtract(request.getAmount())
        );

        bankAccountRepository.save(account);

        return convertToResponse(recordTransaction(
                account,
                WITHDRAWAL,
                request.getAmount(),
                descriptionOrDefault(
                        request.getDescription(),
                        "Simulated cash withdrawal"
                )
        ));
    }

    // Called when money is moved from the bank into a savings account
    @Transactional
    public void debitForSavingsTransfer(
            String email,
            BigDecimal amount,
            String description) {

        BankAccount account = getAccountEntityForTransfer(email);

        requireBankBalance(account, amount);

        account.setBalance(account.getBalance().subtract(amount));

        bankAccountRepository.save(account);

        recordTransaction(
                account,
                TRANSFER_TO_SAVINGS,
                amount,
                descriptionOrDefault(description, "Transfer to savings")
        );
    }

    // Called when money is withdrawn from savings back to the bank
    @Transactional
    public void creditFromSavingsTransfer(
            String email,
            BigDecimal amount,
            String description) {

        BankAccount account = getAccountEntityForTransfer(email);

        account.setBalance(account.getBalance().add(amount));

        bankAccountRepository.save(account);

        recordTransaction(
                account,
                TRANSFER_FROM_SAVINGS,
                amount,
                descriptionOrDefault(description, "Withdrawal from savings")
        );
    }

    // Full simulated bank statement, newest first
    @Transactional(readOnly = true)
    public List<BankTransactionResponse> getTransactions(String email) {

        User user = getUserByEmail(email);

        return bankTransactionRepository
                .findByBankAccountUserIdOrderByTransactionDateDesc(
                        user.getId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private void requireBankBalance(
            BankAccount account,
            BigDecimal amount) {

        if (account.getBalance().compareTo(amount) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient simulated bank balance. You have "
                            + account.getBalance()
                            + " available."
            );
        }
    }

    private BankTransaction recordTransaction(
            BankAccount account,
            String transactionType,
            BigDecimal amount,
            String description) {

        BankTransaction transaction = new BankTransaction();

        transaction.setBankAccount(account);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description);

        return bankTransactionRepository.save(transaction);
    }

    private BankAccount getAccountEntity(String email) {

        User user = getUserByEmail(email);

        return bankAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No simulated bank account yet. Create one first."
                ));
    }

    private BankAccount getAccountEntityForTransfer(String email) {

        User user = getUserByEmail(email);

        return bankAccountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Set up your simulated bank account before moving money."
                ));
    }

    private String generateAccountNumber() {

        return "SSBK" + String.format(
                Locale.ROOT,
                "%010d",
                RANDOM.nextLong(10_000_000_000L)
        );
    }

    private String descriptionOrDefault(
            String description,
            String fallback) {

        return description == null || description.isBlank()
                ? fallback
                : description;
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private BankAccountResponse convertToResponse(BankAccount account) {

        return new BankAccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private BankTransactionResponse convertToResponse(
            BankTransaction transaction) {

        return new BankTransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getBalanceAfter(),
                transaction.getTransactionDate()
        );
    }
}
