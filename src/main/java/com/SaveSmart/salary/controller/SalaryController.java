package com.SaveSmart.salary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.salary.dto.SalaryRequest;
import com.SaveSmart.salary.dto.SalaryResponse;
import com.SaveSmart.salary.service.SalaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/salary")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    // Add salary details
    @PostMapping
    public ResponseEntity<SalaryResponse> addSalary(
            Authentication authentication,
            @Valid @RequestBody SalaryRequest request) {

        SalaryResponse response = salaryService.addSalary(
                authentication.getName(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get salary details
    @GetMapping
    public ResponseEntity<SalaryResponse> getSalary(
            Authentication authentication) {

        SalaryResponse response = salaryService.getSalary(
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    // Update salary details
    @PutMapping
    public ResponseEntity<SalaryResponse> updateSalary(
            Authentication authentication,
            @Valid @RequestBody SalaryRequest request) {

        SalaryResponse response = salaryService.updateSalary(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok(response);
    }
}