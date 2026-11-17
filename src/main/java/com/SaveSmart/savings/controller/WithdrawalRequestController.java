package com.SaveSmart.savings.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.savings.dto.WithdrawalRequestCreate;
import com.SaveSmart.savings.dto.WithdrawalRequestResponse;
import com.SaveSmart.savings.dto.WithdrawalReviewRequest;
import com.SaveSmart.savings.service.WithdrawalRequestService;

import jakarta.validation.Valid;

/**
 * Secured withdrawal flow.
 *
 * User endpoints: create a request, list your requests, cancel a pending one.
 * Review endpoints (ADMIN role only): read the queue, approve, reject.
 */
@RestController
@RequestMapping("/api/savings/withdrawal-requests")
public class WithdrawalRequestController {

    private final WithdrawalRequestService withdrawalRequestService;

    public WithdrawalRequestController(
            WithdrawalRequestService withdrawalRequestService) {

        this.withdrawalRequestService = withdrawalRequestService;
    }

    // Submit a withdrawal request with a reason. No money moves yet.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WithdrawalRequestResponse createRequest(
            Authentication authentication,
            @Valid @RequestBody WithdrawalRequestCreate request) {

        return withdrawalRequestService.createRequest(
                authentication.getName(),
                request
        );
    }

    // The logged-in user's own requests and their status.
    @GetMapping
    public List<WithdrawalRequestResponse> getMyRequests(
            Authentication authentication) {

        return withdrawalRequestService.getMyRequests(
                authentication.getName()
        );
    }

    // Cancel a request that has not been reviewed yet.
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRequest(
            Authentication authentication,
            @PathVariable Long requestId) {

        withdrawalRequestService.cancelRequest(
                authentication.getName(),
                requestId
        );
    }

    // Review queue. Defaults to PENDING; pass ?status=ALL for the history.
    @GetMapping("/review")
    public List<WithdrawalRequestResponse> getReviewQueue(
            @RequestParam(required = false) String status) {

        return withdrawalRequestService.getRequestsForReview(status);
    }

    // Approve a request. This is what moves savings -> simulated bank.
    @PostMapping("/review/{requestId}/approve")
    public WithdrawalRequestResponse approve(
            Authentication authentication,
            @PathVariable Long requestId,
            @RequestBody(required = false) WithdrawalReviewRequest review) {

        return withdrawalRequestService.approve(
                authentication.getName(),
                requestId,
                review == null ? new WithdrawalReviewRequest() : review
        );
    }

    // Reject a request with a mandatory note. No money moves.
    @PostMapping("/review/{requestId}/reject")
    public WithdrawalRequestResponse reject(
            Authentication authentication,
            @PathVariable Long requestId,
            @RequestBody(required = false) WithdrawalReviewRequest review) {

        return withdrawalRequestService.reject(
                authentication.getName(),
                requestId,
                review == null ? new WithdrawalReviewRequest() : review
        );
    }
}
