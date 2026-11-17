
package com.SaveSmart.savings.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.savings.entity.SavingsGoal;

public interface SavingsGoalRepository
        extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

}
