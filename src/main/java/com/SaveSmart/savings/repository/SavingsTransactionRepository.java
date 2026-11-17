
package com.SaveSmart.savings.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.savings.entity.SavingsTransaction;

public interface SavingsTransactionRepository
        extends JpaRepository<SavingsTransaction, Long> {

    List<SavingsTransaction> findBySavingsAccountIdOrderByTransactionDateDesc(
            Long savingsAccountId
    );

    // Get all transactions of a user between two timestamps
    List<SavingsTransaction>
            findBySavingsAccountUserIdAndTransactionDateBetween(
                    Long userId,
                    LocalDateTime startDate,
                    LocalDateTime endDate
            );

    // Get the five most recent transactions of a user
    List<SavingsTransaction>
            findTop5BySavingsAccountUserIdOrderByTransactionDateDesc(
                    Long userId
            );
}
