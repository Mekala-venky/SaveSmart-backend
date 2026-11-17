package com.SaveSmart.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.bank.entity.BankAccount;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
