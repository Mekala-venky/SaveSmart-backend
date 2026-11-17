
package com.SaveSmart.expense.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.expense.dto.ExpenseRequest;
import com.SaveSmart.expense.dto.ExpenseResponse;
import com.SaveSmart.expense.entity.Expense;
import com.SaveSmart.expense.repository.ExpenseRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // Add a new expense
    public ExpenseResponse addExpense(
            String email,
            ExpenseRequest request) {

        User user = getUserByEmail(email);

        Expense expense = new Expense();

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);

        return convertToResponse(savedExpense);
    }

    // Get all expenses of the logged-in user
    public List<ExpenseResponse> getExpenses(String email) {

        User user = getUserByEmail(email);

        return expenseRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Update an existing expense
    public ExpenseResponse updateExpense(
            String email,
            Long expenseId,
            ExpenseRequest request) {

        User user = getUserByEmail(email);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Expense not found"
                ));

        // Ensure the expense belongs to the logged-in user
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Expense not found"
            );
        }

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        Expense updatedExpense = expenseRepository.save(expense);

        return convertToResponse(updatedExpense);
    }

    // Delete an existing expense
    public void deleteExpense(String email, Long expenseId) {

        User user = getUserByEmail(email);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Expense not found"
                ));

        // Ensure the expense belongs to the logged-in user
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Expense not found"
            );
        }

        expenseRepository.delete(expense);
    }

    // Find user by email
    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    // Convert Expense entity into ExpenseResponse
    private ExpenseResponse convertToResponse(Expense expense) {

        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getCreatedAt()
        );
    }
}
