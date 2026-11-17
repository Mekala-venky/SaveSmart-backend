package com.SaveSmart.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class SalaryRequest {

    @NotNull(message = "Monthly salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal monthlySalary;

    @NotBlank(message = "Salary source is required")
    @Size(max = 100)
    private String salarySource;

    @NotNull(message = "Salary received date is required")
    @PastOrPresent(message = "Salary date cannot be in the future")
    private LocalDate salaryReceivedDate;

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public String getSalarySource() {
        return salarySource;
    }

    public void setSalarySource(String salarySource) {
        this.salarySource = salarySource;
    }

    public LocalDate getSalaryReceivedDate() {
        return salaryReceivedDate;
    }

    public void setSalaryReceivedDate(LocalDate salaryReceivedDate) {
        this.salaryReceivedDate = salaryReceivedDate;
    }
}