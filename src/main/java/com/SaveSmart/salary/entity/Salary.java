package com.SaveSmart.salary.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SaveSmart.user.entity.User;

import jakarta.persistence.*;

@Entity
@Table(name = "salaries")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlySalary;

    @Column(nullable = false, length = 100)
    private String salarySource;

    @Column(nullable = false)
    private LocalDate salaryReceivedDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}