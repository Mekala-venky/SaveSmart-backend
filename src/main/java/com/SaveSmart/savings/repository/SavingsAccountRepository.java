
package com.SaveSmart.savings.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.savings.entity.SavingsAccount;

public interface SavingsAccountRepository
        extends JpaRepository<SavingsAccount, Long> {

    List<SavingsAccount> findByUserId(Long userId);

    Optional<SavingsAccount> findByUserIdAndAccountType(
            Long userId,
            String accountType
    );

    boolean existsByUserIdAndAccountType(
            Long userId,
            String accountType
    );
}
