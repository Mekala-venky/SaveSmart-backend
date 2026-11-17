package com.SaveSmart.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SalaryResponse {

    private Long id;
    private BigDecimal monthlySalary;
    private String salarySource;
    private LocalDate salaryReceivedDate;
    private LocalDateTime createdAt;

    public SalaryResponse(
            Long id,
            BigDecimal monthlySalary,
            String salarySource,
            LocalDate salaryReceivedDate,
            LocalDateTime createdAt) {

        this.id = id;
        this.monthlySalary = monthlySalary;
        this.salarySource = salarySource;
        this.salaryReceivedDate = salaryReceivedDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public String getSalarySource() {
        return salarySource;
    }

    public LocalDate getSalaryReceivedDate() {
        return salaryReceivedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}