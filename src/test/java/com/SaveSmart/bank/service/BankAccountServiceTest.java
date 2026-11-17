package com.SaveSmart.bank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.dto.BankAccountRequest;
import com.SaveSmart.bank.dto.BankTransactionRequest;
import com.SaveSmart.bank.dto.BankTransactionResponse;
import com.SaveSmart.bank.entity.BankAccount;
import com.SaveSmart.bank.entity.BankTransaction;
import com.SaveSmart.bank.repository.BankAccountRepository;
import com.SaveSmart.bank.repository.BankTransactionRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    private static final String EMAIL = "asha@example.com";
    private static final Long USER_ID = 7L;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private UserRepository userRepository;

    private BankAccountService service;

    @BeforeEach
    void setUp() {
        service = new BankAccountService(
                bankAccountRepository,
                bankTransactionRepository,
                userRepository
        );
    }

    @Test
    void createsSimulatedAccountWithOpeningBalance() {
        stubUser();
        when(bankAccountRepository.existsByUserId(USER_ID)).thenReturn(false);
        when(bankTransactionRepository.save(any(BankTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccountRequest request = new BankAccountRequest();
        request.setOpeningBalance(new BigDecimal("100000.00"));

        var response = service.createAccount(EMAIL, request);

        assertThat(response.getBalance())
                .isEqualByComparingTo("100000.00");
        assertThat(response.getAccountNumber()).startsWith("SSBK");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void refusesASecondSimulatedAccount() {
        stubUser();
        when(bankAccountRepository.existsByUserId(USER_ID)).thenReturn(true);

        BankAccountRequest request = new BankAccountRequest();
        request.setOpeningBalance(BigDecimal.ZERO);

        ResponseStatusException error = catchThrowableOfType(
                () -> service.createAccount(EMAIL, request),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void depositAddsSimulatedMoney() {
        BankAccount account = stubAccount("5000.00");
        stubTransactionSave();

        BankTransactionRequest request = new BankTransactionRequest();
        request.setAmount(new BigDecimal("2500.00"));

        BankTransactionResponse response = service.deposit(EMAIL, request);

        assertThat(account.getBalance()).isEqualByComparingTo("7500.00");
        assertThat(response.getTransactionType())
                .isEqualTo(BankAccountService.DEPOSIT);
        assertThat(response.getBalanceAfter())
                .isEqualByComparingTo("7500.00");
    }

    @Test
    void withdrawRejectsMoreThanTheBankBalance() {
        BankAccount account = stubAccount("1000.00");

        BankTransactionRequest request = new BankTransactionRequest();
        request.setAmount(new BigDecimal("1500.00"));

        ResponseStatusException error = catchThrowableOfType(
                () -> service.withdraw(EMAIL, request),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.getReason())
                .contains("Insufficient simulated bank balance");
        assertThat(account.getBalance()).isEqualByComparingTo("1000.00");
        verify(bankTransactionRepository, never())
                .save(any(BankTransaction.class));
    }

    @Test
    void savingToSavingsDebitsTheBankAccount() {
        BankAccount account = stubAccount("4000.00");
        stubTransactionSave();

        service.debitForSavingsTransfer(
                EMAIL,
                new BigDecimal("1500.00"),
                "To future savings"
        );

        assertThat(account.getBalance()).isEqualByComparingTo("2500.00");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void savingMoreThanTheBankBalanceIsRejected() {
        BankAccount account = stubAccount("500.00");

        ResponseStatusException error = catchThrowableOfType(
                () -> service.debitForSavingsTransfer(
                        EMAIL,
                        new BigDecimal("900.00"),
                        "To future savings"
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(account.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void approvedWithdrawalCreditsTheBankAccount() {
        BankAccount account = stubAccount("3000.00");
        stubTransactionSave();

        service.creditFromSavingsTransfer(
                EMAIL,
                new BigDecimal("1200.00"),
                "From future savings"
        );

        assertThat(account.getBalance()).isEqualByComparingTo("4200.00");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void transfersRequireABankAccountToExist() {
        stubUser();
        when(bankAccountRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        ResponseStatusException error = catchThrowableOfType(
                () -> service.debitForSavingsTransfer(
                        EMAIL,
                        new BigDecimal("100.00"),
                        "To future savings"
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.getReason()).contains("Set up your simulated bank");
    }

    private void stubUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setFullName("Asha Rao");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private BankAccount stubAccount(String balance) {
        stubUser();

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setFullName("Asha Rao");

        BankAccount account = new BankAccount();
        account.setId(5L);
        account.setUser(user);
        account.setAccountNumber("SSBK0000000005");
        account.setBalance(new BigDecimal(balance));

        when(bankAccountRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(account));

        return account;
    }

    private void stubTransactionSave() {
        when(bankTransactionRepository.save(any(BankTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
