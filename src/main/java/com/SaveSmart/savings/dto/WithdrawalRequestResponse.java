package com.SaveSmart.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WithdrawalRequestResponse {

    private Long id;
    private Long savingsAccountId;
    private String accountType;
    private BigDecimal amount;
    private String description;
    private String status;
    private String reviewerNote;
    private String reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String requesterName;
    private String requesterEmail;

    // Review helpers: what would be left, and anything that looks unusual.
    private BigDecimal remainingBalance;
    private List<String> riskFlags;

    public WithdrawalRequestResponse(
            Long id,
            Long savingsAccountId,
            String accountType,
            BigDecimal amount,
            String description,
            String status,
            String reviewerNote,
            String reviewedBy,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt,
            String requesterName,
            String requesterEmail,
            BigDecimal remainingBalance,
            List<String> riskFlags) {

        this.id = id;
        this.savingsAccountId = savingsAccountId;
        this.accountType = accountType;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.reviewerNote = reviewerNote;
        this.reviewedBy = reviewedBy;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.remainingBalance = remainingBalance;
        this.riskFlags = riskFlags == null ? List.of() : riskFlags;
    }

    public Long getId() {
        return id;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getReviewerNote() {
        return reviewerNote;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public List<String> getRiskFlags() {
        return riskFlags;
    }
}
