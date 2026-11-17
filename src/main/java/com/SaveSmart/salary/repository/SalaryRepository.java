package com.SaveSmart.salary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SaveSmart.salary.entity.Salary;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    Optional<Salary> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}