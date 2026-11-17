package com.SaveSmart.savings.dto;

import jakarta.validation.constraints.Size;

/**
 * A reviewer may explain the decision. The note is required when rejecting so
 * the user knows why the money could not be released.
 */
public class WithdrawalReviewRequest {

    @Size(max = 255, message = "Reviewer note cannot exceed 255 characters")
    private String reviewerNote;

    public String getReviewerNote() {
        return reviewerNote;
    }

    public void setReviewerNote(String reviewerNote) {
        this.reviewerNote = reviewerNote;
    }
}
