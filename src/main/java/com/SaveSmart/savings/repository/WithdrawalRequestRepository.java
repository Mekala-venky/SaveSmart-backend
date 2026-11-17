package com.SaveSmart.savings.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.savings.entity.WithdrawalRequest;
import com.SaveSmart.savings.entity.WithdrawalStatus;

public interface WithdrawalRequestRepository
        extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<WithdrawalRequest> findByStatusOrderByCreatedAtAsc(
            WithdrawalStatus status);

    List<WithdrawalRequest> findAllByOrderByCreatedAtDesc();

    Optional<WithdrawalRequest> findByIdAndUserId(Long id, Long userId);

    boolean existsBySavingsAccountIdAndStatus(
            Long savingsAccountId,
            WithdrawalStatus status);

    long countByUserIdAndStatusAndIdNot(
            Long userId,
            WithdrawalStatus status,
            Long id);

    long countByUserIdAndStatusAndReviewedAtAfter(
            Long userId,
            WithdrawalStatus status,
            java.time.LocalDateTime after);
}
