
package com.SaveSmart.savings.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.SaveSmart.savings.dto.SavingsGoalContributionRequest;
import com.SaveSmart.savings.dto.SavingsGoalRequest;
import com.SaveSmart.savings.dto.SavingsGoalResponse;
import com.SaveSmart.savings.service.SavingsGoalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/savings/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(
            SavingsGoalService savingsGoalService) {

        this.savingsGoalService = savingsGoalService;
    }

    // Create a savings goal
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoalResponse createGoal(
            Authentication authentication,
            @Valid @RequestBody SavingsGoalRequest request) {

        String email = authentication.getName();

        return savingsGoalService.createGoal(email, request);
    }

    // Get all savings goals
    @GetMapping
    public List<SavingsGoalResponse> getGoals(
            Authentication authentication) {

        String email = authentication.getName();

        return savingsGoalService.getGoals(email);
    }

    // Get a single savings goal
    @GetMapping("/{goalId}")
    public SavingsGoalResponse getGoal(
            Authentication authentication,
            @PathVariable Long goalId) {

        String email = authentication.getName();

        return savingsGoalService.getGoal(email, goalId);
    }

    // Update a savings goal
    @PutMapping("/{goalId}")
    public SavingsGoalResponse updateGoal(
            Authentication authentication,
            @PathVariable Long goalId,
            @Valid @RequestBody SavingsGoalRequest request) {

        String email = authentication.getName();

        return savingsGoalService.updateGoal(
                email, goalId, request
        );
    }

    // Add money toward a savings goal
    @PostMapping("/{goalId}/add-money")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoalResponse addMoney(
            Authentication authentication,
            @PathVariable Long goalId,
            @Valid @RequestBody SavingsGoalContributionRequest request) {

        String email = authentication.getName();

        return savingsGoalService.addMoney(
                email, goalId, request
        );
    }

    // Delete a savings goal
    @DeleteMapping("/{goalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(
            Authentication authentication,
            @PathVariable Long goalId) {

        String email = authentication.getName();

        savingsGoalService.deleteGoal(email, goalId);
    }
}
