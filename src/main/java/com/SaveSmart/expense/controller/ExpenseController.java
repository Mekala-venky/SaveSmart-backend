
package com.SaveSmart.expense.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.SaveSmart.expense.dto.ExpenseRequest;
import com.SaveSmart.expense.dto.ExpenseResponse;
import com.SaveSmart.expense.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Add a new expense
    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            Authentication authentication,
            @Valid @RequestBody ExpenseRequest request) {

        ExpenseResponse response = expenseService.addExpense(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all expenses of the logged-in user
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            Authentication authentication) {

        List<ExpenseResponse> expenses =
                expenseService.getExpenses(authentication.getName());

        return ResponseEntity.ok(expenses);
    }

    // Update an existing expense
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            Authentication authentication,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequest request) {

        ExpenseResponse response = expenseService.updateExpense(
                authentication.getName(),
                expenseId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // Delete an existing expense
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            Authentication authentication,
            @PathVariable Long expenseId) {

        expenseService.deleteExpense(
                authentication.getName(),
                expenseId
        );

        return ResponseEntity.noContent().build();
    }
}
