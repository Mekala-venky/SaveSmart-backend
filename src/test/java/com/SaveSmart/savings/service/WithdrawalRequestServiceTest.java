package com.SaveSmart.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.bank.repository.BankAccountRepository;
import com.SaveSmart.bank.service.BankAccountService;
import com.SaveSmart.savings.dto.WithdrawalRequestCreate;
import com.SaveSmart.savings.dto.WithdrawalRequestResponse;
import com.SaveSmart.savings.dto.WithdrawalReviewRequest;
import com.SaveSmart.savings.entity.SavingsAccount;
import com.SaveSmart.savings.entity.SavingsTransaction;
import com.SaveSmart.savings.entity.WithdrawalRequest;
import com.SaveSmart.savings.entity.WithdrawalStatus;
import com.SaveSmart.savings.repository.SavingsAccountRepository;
import com.SaveSmart.savings.repository.SavingsTransactionRepository;
import com.SaveSmart.savings.repository.WithdrawalRequestRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class WithdrawalRequestServiceTest {

    private static final String USER_EMAIL = "asha@example.com";
    private static final String REVIEWER_EMAIL = "reviewer@savesmart.app";
    private static final Long USER_ID = 7L;
    private static final Long ACCOUNT_ID = 12L;

    @Mock
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @Mock
    private SavingsTransactionRepository savingsTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    private WithdrawalRequestService service;

    private User user;
    private SavingsAccount account;

    @BeforeEach
    void setUp() {
        service = new WithdrawalRequestService(
                withdrawalRequestRepository,
                savingsAccountRepository,
                savingsTransactionRepository,
                userRepository,
                bankAccountService,
                bankAccountRepository
        );

        user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setFullName("Asha Rao");

        account = new SavingsAccount();
        account.setId(ACCOUNT_ID);
        account.setAccountType("FUTURE_SAVINGS");
        account.setBalance(new BigDecimal("5000.00"));
        account.setUser(user);
    }

    @Test
    void creatingARequestDoesNotMoveMoney() {
        stubUser();
        when(bankAccountRepository.existsByUserId(USER_ID)).thenReturn(true);
        when(savingsAccountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.existsBySavingsAccountIdAndStatus(
                ACCOUNT_ID,
                WithdrawalStatus.PENDING
        )).thenReturn(false);
        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestResponse response = service.createRequest(
                USER_EMAIL,
                createRequest("Hospital bill for father", "1200.00")
        );

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getRemainingBalance())
                .isEqualByComparingTo("3800.00");
        assertThat(account.getBalance()).isEqualByComparingTo("5000.00");
        verify(savingsAccountRepository, never()).save(any());
        verify(bankAccountService, never())
                .creditFromSavingsTransfer(anyString(), any(), anyString());
    }

    @Test
    void requestCannotExceedTheSavingsBalance() {
        stubUser();
        when(bankAccountRepository.existsByUserId(USER_ID)).thenReturn(true);
        when(savingsAccountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(account));

        ResponseStatusException error = catchThrowableOfType(
                () -> service.createRequest(
                        USER_EMAIL,
                        createRequest("Hospital bill for father", "9000.00")
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(withdrawalRequestRepository, never())
                .save(any(WithdrawalRequest.class));
    }

    @Test
    void onlyOnePendingRequestPerAccountIsAllowed() {
        stubUser();
        when(bankAccountRepository.existsByUserId(USER_ID)).thenReturn(true);
        when(savingsAccountRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.existsBySavingsAccountIdAndStatus(
                ACCOUNT_ID,
                WithdrawalStatus.PENDING
        )).thenReturn(true);

        ResponseStatusException error = catchThrowableOfType(
                () -> service.createRequest(
                        USER_EMAIL,
                        createRequest("Hospital bill for father", "500.00")
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.getReason()).contains("waiting for review");
    }

    @Test
    void approvalMovesSavingsBackToTheSimulatedBank() {
        WithdrawalRequest request = pendingRequest("1200.00");
        stubReview(request);

        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(savingsTransactionRepository.save(any(SavingsTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestResponse response = service.approve(
                REVIEWER_EMAIL,
                request.getId(),
                review("Verified hospital bill")
        );

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getReviewedBy()).isEqualTo(REVIEWER_EMAIL);
        assertThat(account.getBalance()).isEqualByComparingTo("3800.00");

        verify(bankAccountService).creditFromSavingsTransfer(
                eq(USER_EMAIL),
                eq(new BigDecimal("1200.00")),
                contains("future savings")
        );

        ArgumentCaptor<SavingsTransaction> transaction =
                ArgumentCaptor.forClass(SavingsTransaction.class);

        verify(savingsTransactionRepository).save(transaction.capture());

        assertThat(transaction.getValue().getTransactionType())
                .isEqualTo("WITHDRAWAL");
        assertThat(transaction.getValue().getAmount())
                .isEqualByComparingTo("1200.00");
        assertThat(transaction.getValue().getDescription())
                .contains("Hospital bill for father");
    }

    @Test
    void approvalIsBlockedWhenTheBalanceDropped() {
        WithdrawalRequest request = pendingRequest("4800.00");
        account.setBalance(new BigDecimal("1000.00"));
        stubReview(request);

        ResponseStatusException error = catchThrowableOfType(
                () -> service.approve(
                        REVIEWER_EMAIL,
                        request.getId(),
                        review(null)
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(error.getReason()).contains("still pending");
        assertThat(account.getBalance()).isEqualByComparingTo("1000.00");
        verify(bankAccountService, never())
                .creditFromSavingsTransfer(anyString(), any(), anyString());
        verify(savingsTransactionRepository, never()).save(any());
    }

    @Test
    void aUserCannotApproveTheirOwnRequest() {
        WithdrawalRequest request = pendingRequest("1200.00");
        when(withdrawalRequestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));

        ResponseStatusException error = catchThrowableOfType(
                () -> service.approve(
                        USER_EMAIL,
                        request.getId(),
                        review(null)
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(account.getBalance()).isEqualByComparingTo("5000.00");
    }

    @Test
    void aRequestCanOnlyBeReviewedOnce() {
        WithdrawalRequest request = pendingRequest("1200.00");
        request.setStatus(WithdrawalStatus.APPROVED);

        when(withdrawalRequestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));

        ResponseStatusException error = catchThrowableOfType(
                () -> service.approve(
                        REVIEWER_EMAIL,
                        request.getId(),
                        review(null)
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.getReason()).contains("already approved");
    }

    @Test
    void rejectionRequiresANoteAndNeverMovesMoney() {
        WithdrawalRequest request = pendingRequest("1200.00");
        stubReview(request);

        ResponseStatusException error = catchThrowableOfType(
                () -> service.reject(
                        REVIEWER_EMAIL,
                        request.getId(),
                        review("   ")
                ),
                ResponseStatusException.class
        );

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(account.getBalance()).isEqualByComparingTo("5000.00");
        verify(savingsAccountRepository, never()).save(any());
        verify(bankAccountService, never())
                .creditFromSavingsTransfer(anyString(), any(), anyString());
    }

    @Test
    void rejectionRecordsTheReason() {
        WithdrawalRequest request = pendingRequest("1200.00");
        stubReview(request);

        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestResponse response = service.reject(
                REVIEWER_EMAIL,
                request.getId(),
                review("Please use your emergency fund instead")
        );

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getReviewerNote())
                .isEqualTo("Please use your emergency fund instead");
        assertThat(account.getBalance()).isEqualByComparingTo("5000.00");
        verify(savingsAccountRepository, never()).save(any());
    }

    @Test
    void pendingRequestsShowRiskHintsToTheReviewer() {
        WithdrawalRequest request = pendingRequest("4900.00");
        request.setDescription("Need money");

        stubUser();
        when(withdrawalRequestRepository
                .findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(request));

        List<WithdrawalRequestResponse> requests =
                service.getMyRequests(USER_EMAIL);

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRiskFlags())
                .contains(
                        "Leaves less than 10% in this account",
                        "Reason is short — ask for more detail",
                        "Reason looks generic — verify it before approving"
                );
    }

    @Test
    void cancellingRemovesOnlyAPendingRequest() {
        WithdrawalRequest request = pendingRequest("1200.00");

        stubUser();
        when(withdrawalRequestRepository.findByIdAndUserId(
                request.getId(),
                USER_ID
        )).thenReturn(Optional.of(request));

        service.cancelRequest(USER_EMAIL, request.getId());

        verify(withdrawalRequestRepository).delete(request);
    }

    private void stubUser() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));
    }

    private void stubReview(WithdrawalRequest request) {
        when(withdrawalRequestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));
    }

    private WithdrawalRequest pendingRequest(String amount) {
        WithdrawalRequest request = new WithdrawalRequest();

        request.setId(99L);
        request.setUser(user);
        request.setSavingsAccount(account);
        request.setAmount(new BigDecimal(amount));
        request.setDescription("Hospital bill for father");
        request.setStatus(WithdrawalStatus.PENDING);

        return request;
    }

    private WithdrawalRequestCreate createRequest(
            String description,
            String amount) {

        WithdrawalRequestCreate request = new WithdrawalRequestCreate();

        request.setSavingsAccountId(ACCOUNT_ID);
        request.setAmount(new BigDecimal(amount));
        request.setDescription(description);

        return request;
    }

    private WithdrawalReviewRequest review(String note) {
        WithdrawalReviewRequest review = new WithdrawalReviewRequest();

        review.setReviewerNote(note);

        return review;
    }
}
