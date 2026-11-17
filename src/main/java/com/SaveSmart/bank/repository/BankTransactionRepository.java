package com.SaveSmart.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.bank.entity.BankTransaction;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByBankAccountUserIdOrderByTransactionDateDesc(
            Long userId);
}
