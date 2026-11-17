package com.SaveSmart.savings.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

/**
 * Secured withdrawals.
 *
 * A user cannot move money out of savings directly. They create a request with
 * a written reason, a reviewer inspects the request (with risk hints) and only
 * an approval moves money back to the simulated bank account.
 */
@Service
public class WithdrawalRequestService {

    private static final BigDecimal MINIMUM_REMAINING_RATIO =
            new BigDecimal("0.10");

    private static final int RECENT_WITHDRAWAL_LIMIT = 3;
    private static final int RECENT_WITHDRAWAL_DAYS = 30;
    private static final int SHORT_REASON_LENGTH = 20;

    private static final List<String> GENERIC_REASONS = List.of(
            "need money",
            "need cash",
            "personal use",
            "personal reasons",
            "no reason",
            "misc",
            "asdf",
            "test"
    );

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;

    public WithdrawalRequestService(
            WithdrawalRequestRepository withdrawalRequestRepository,
            SavingsAccountRepository savingsAccountRepository,
            SavingsTransactionRepository savingsTransactionRepository,
            UserRepository userRepository,
            BankAccountService bankAccountService,
            BankAccountRepository bankAccountRepository) {

        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.userRepository = userRepository;
        this.bankAccountService = bankAccountService;
        this.bankAccountRepository = bankAccountRepository;
    }

    // Step 1: the user explains why the money is needed.
    @Transactional
    public WithdrawalRequestResponse createRequest(
            String email,
            WithdrawalRequestCreate request) {

        User user = getUserByEmail(email);

        // Money must have somewhere to go after approval.
        if (!bankAccountRepository.existsByUserId(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Set up your simulated bank account before requesting a withdrawal"
            );
        }

        SavingsAccount account = savingsAccountRepository
                .findById(request.getSavingsAccountId())
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

        if (account.getBalance().compareTo(request.getAmount()) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "That amount is more than the savings balance of "
                            + account.getBalance()
            );
        }

        // One open request per account keeps the review queue honest.
        if (withdrawalRequestRepository.existsBySavingsAccountIdAndStatus(
                account.getId(),
                WithdrawalStatus.PENDING)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have a withdrawal request waiting for review "
                            + "on this account. Cancel it first or wait for a decision."
            );
        }

        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();

        withdrawalRequest.setUser(user);
        withdrawalRequest.setSavingsAccount(account);
        withdrawalRequest.setAmount(request.getAmount());
        withdrawalRequest.setDescription(request.getDescription().trim());
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);

        WithdrawalRequest saved =
                withdrawalRequestRepository.save(withdrawalRequest);

        return convertToResponse(saved, user);
    }

    // Step 2: the user follows the status of their own requests.
    @Transactional(readOnly = true)
    public List<WithdrawalRequestResponse> getMyRequests(String email) {

        User user = getUserByEmail(email);

        return withdrawalRequestRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(request -> convertToResponse(request, user))
                .toList();
    }

    // Step 3: a reviewer reads the queue. Status is optional: PENDING by default.
    @Transactional(readOnly = true)
    public List<WithdrawalRequestResponse> getRequestsForReview(
            String status) {

        List<WithdrawalRequest> requests;

        if (status == null || status.isBlank()
                || "ALL".equalsIgnoreCase(status)) {

            requests = withdrawalRequestRepository
                    .findAllByOrderByCreatedAtDesc();
        } else {
            requests = withdrawalRequestRepository
                    .findByStatusOrderByCreatedAtAsc(parseStatus(status));
        }

        return requests.stream()
                .map(request -> convertToResponse(
                        request,
                        request.getUser()
                ))
                .toList();
    }

    // Step 4a: approval is the only path that releases money.
    @Transactional
    public WithdrawalRequestResponse approve(
            String reviewerEmail,
            Long requestId,
            WithdrawalReviewRequest review) {

        WithdrawalRequest request = getRequest(requestId);

        requirePending(request);
        requireDifferentReviewer(request, reviewerEmail);

        SavingsAccount account = request.getSavingsAccount();

        // The balance may have changed while the request was waiting.
        if (account.getBalance().compareTo(request.getAmount()) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient savings balance now ("
                            + account.getBalance()
                            + "). The request is still pending; reject it or "
                            + "wait for new deposits."
            );
        }

        // Move money out of savings...
        account.setBalance(
                account.getBalance().subtract(request.getAmount())
        );
        savingsAccountRepository.save(account);

        // ...and back into the simulated bank account.
        bankAccountService.creditFromSavingsTransfer(
                request.getUser().getEmail(),
                request.getAmount(),
                SavingsTransactionService.bankDescription(
                        "From",
                        account.getAccountType(),
                        request.getDescription()
                )
        );

        // Record it in the savings ledger.
        SavingsTransaction transaction = new SavingsTransaction();

        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(request.getAmount());
        transaction.setDescription(
                truncate("Approved withdrawal · " + request.getDescription())
        );
        transaction.setSavingsAccount(account);

        savingsTransactionRepository.save(transaction);

        request.setStatus(WithdrawalStatus.APPROVED);
        request.setReviewedBy(reviewerEmail);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewerNote(normaliseNote(review.getReviewerNote()));

        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        return convertToResponse(saved, saved.getUser());
    }

    // Step 4b: rejection never moves money, but must explain why.
    @Transactional
    public WithdrawalRequestResponse reject(
            String reviewerEmail,
            Long requestId,
            WithdrawalReviewRequest review) {

        WithdrawalRequest request = getRequest(requestId);

        requirePending(request);
        requireDifferentReviewer(request, reviewerEmail);

        String note = normaliseNote(review.getReviewerNote());

        if (note == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add a short note so the user knows why the request was rejected"
            );
        }

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setReviewedBy(reviewerEmail);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewerNote(note);

        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        return convertToResponse(saved, saved.getUser());
    }

    // The user may withdraw a request while it is still waiting.
    @Transactional
    public void cancelRequest(String email, Long requestId) {

        User user = getUserByEmail(email);

        WithdrawalRequest request = withdrawalRequestRepository
                .findByIdAndUserId(requestId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Withdrawal request not found"
                ));

        if (request.getStatus() != WithdrawalStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending requests can be cancelled"
            );
        }

        withdrawalRequestRepository.delete(request);
    }

    private WithdrawalRequest getRequest(Long requestId) {

        return withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Withdrawal request not found"
                ));
    }

    private void requirePending(WithdrawalRequest request) {

        if (request.getStatus() != WithdrawalStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This request was already "
                            + request.getStatus().name().toLowerCase()
                            + " and cannot be reviewed again"
            );
        }
    }

    // Separation of duties: nobody can approve their own withdrawal.
    private void requireDifferentReviewer(
            WithdrawalRequest request,
            String reviewerEmail) {

        if (request.getUser().getEmail().equalsIgnoreCase(reviewerEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot review your own withdrawal request"
            );
        }
    }

    private WithdrawalStatus parseStatus(String value) {

        try {
            return WithdrawalStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PENDING, APPROVED, REJECTED or ALL"
            );
        }
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private String normaliseNote(String note) {

        return note == null || note.isBlank() ? null : note.trim();
    }

    private String truncate(String value) {

        return value.length() <= 255 ? value : value.substring(0, 255);
    }

    /**
     * Hints shown to the reviewer. They never approve or reject by themselves;
     * they simply surface patterns that deserve a closer look.
     */
    private List<String> buildRiskFlags(WithdrawalRequest request) {

        List<String> flags = new ArrayList<>();

        if (request.getStatus() != WithdrawalStatus.PENDING) {
            return flags;
        }

        SavingsAccount account = request.getSavingsAccount();
        BigDecimal balance = account.getBalance();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(balance) > 0) {

            flags.add("Amount is higher than the current balance");
        } else {

            BigDecimal remaining = balance.subtract(amount);
            BigDecimal minimum = balance
                    .multiply(MINIMUM_REMAINING_RATIO)
                    .setScale(2, RoundingMode.HALF_UP);

            if (balance.signum() > 0
                    && remaining.compareTo(minimum) < 0) {

                flags.add("Leaves less than 10% in this account");
            }
        }

        String reason = request.getDescription().toLowerCase(Locale.ROOT);

        if (request.getDescription().trim().length() < SHORT_REASON_LENGTH) {
            flags.add("Reason is short — ask for more detail");
        }

        boolean generic = GENERIC_REASONS.stream()
                .anyMatch(reason::contains);

        if (generic) {
            flags.add("Reason looks generic — verify it before approving");
        }

        long otherPending = withdrawalRequestRepository
                .countByUserIdAndStatusAndIdNot(
                        request.getUser().getId(),
                        WithdrawalStatus.PENDING,
                        request.getId()
                );

        if (otherPending > 0) {
            flags.add("This user has other withdrawal requests waiting");
        }

        long recentWithdrawals = withdrawalRequestRepository
                .countByUserIdAndStatusAndReviewedAtAfter(
                        request.getUser().getId(),
                        WithdrawalStatus.APPROVED,
                        LocalDateTime.now()
                                .minusDays(RECENT_WITHDRAWAL_DAYS)
                );

        if (recentWithdrawals >= RECENT_WITHDRAWAL_LIMIT) {

            flags.add(recentWithdrawals
                    + " withdrawals approved in the last "
                    + RECENT_WITHDRAWAL_DAYS + " days");
        }

        return flags;
    }

    private WithdrawalRequestResponse convertToResponse(
            WithdrawalRequest request,
            User user) {

        SavingsAccount account = request.getSavingsAccount();

        BigDecimal remaining = request.getStatus() == WithdrawalStatus.PENDING
                ? account.getBalance().subtract(request.getAmount())
                : null;

        return new WithdrawalRequestResponse(
                request.getId(),
                account.getId(),
                account.getAccountType(),
                request.getAmount(),
                request.getDescription(),
                request.getStatus().name(),
                request.getReviewerNote(),
                request.getReviewedBy(),
                request.getCreatedAt(),
                request.getReviewedAt(),
                user.getFullName(),
                user.getEmail(),
                remaining,
                buildRiskFlags(request)
        );
    }
}
