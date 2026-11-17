
package com.SaveSmart.expense.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Get all expenses belonging to a particular user
    List<Expense> findByUserId(Long userId);

    // Get a user's expenses between two dates
    List<Expense> findByUserIdAndExpenseDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

}
