
package com.SaveSmart.savings.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.savings.dto.SavingsGoalContributionRequest;
import com.SaveSmart.savings.dto.SavingsGoalRequest;
import com.SaveSmart.savings.dto.SavingsGoalResponse;
import com.SaveSmart.savings.entity.SavingsGoal;
import com.SaveSmart.savings.repository.SavingsGoalRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(
            SavingsGoalRepository savingsGoalRepository,
            UserRepository userRepository) {

        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
    }

    // Create a new savings goal
    @Transactional
    public SavingsGoalResponse createGoal(
            String email,
            SavingsGoalRequest request) {

        User user = getUserByEmail(email);

        SavingsGoal goal = new SavingsGoal();

        goal.setGoalName(request.getGoalName().trim());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setTargetDate(request.getTargetDate());
        goal.setDescription(request.getDescription());
        goal.setUser(user);

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);

        return convertToResponse(savedGoal);
    }

    // Get all savings goals of the logged-in user
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoals(String email) {

        User user = getUserByEmail(email);

        return savingsGoalRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get a single savings goal of the logged-in user
    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoal(
            String email,
            Long goalId) {

        SavingsGoal goal = getGoalForUser(email, goalId);

        return convertToResponse(goal);
    }

    // Update an existing savings goal
    @Transactional
    public SavingsGoalResponse updateGoal(
            String email,
            Long goalId,
            SavingsGoalRequest request) {

        SavingsGoal goal = getGoalForUser(email, goalId);

        // The target cannot be lowered below the amount already saved
        if (request.getTargetAmount()
                .compareTo(goal.getCurrentAmount()) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target amount cannot be less than the amount already saved"
            );
        }

        goal.setGoalName(request.getGoalName().trim());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setDescription(request.getDescription());

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);

        return convertToResponse(updatedGoal);
    }

    // Add money toward a savings goal
    @Transactional
    public SavingsGoalResponse addMoney(
            String email,
            Long goalId,
            SavingsGoalContributionRequest request) {

        SavingsGoal goal = getGoalForUser(email, goalId);

        BigDecimal remainingAmount =
                goal.getTargetAmount().subtract(goal.getCurrentAmount());

        // Check whether the goal has already been achieved
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Savings goal is already achieved"
            );
        }

        // Prevent saving more than the remaining target amount
        if (request.getAmount().compareTo(remainingAmount) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount exceeds the remaining goal amount of "
                            + remainingAmount
            );
        }

        goal.setCurrentAmount(
                goal.getCurrentAmount().add(request.getAmount())
        );

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);

        return convertToResponse(updatedGoal);
    }

    // Delete an existing savings goal
    @Transactional
    public void deleteGoal(String email, Long goalId) {

        SavingsGoal goal = getGoalForUser(email, goalId);

        savingsGoalRepository.delete(goal);
    }

    // Find the goal and verify that it belongs to the logged-in user
    private SavingsGoal getGoalForUser(
            String email,
            Long goalId) {

        User user = getUserByEmail(email);

        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Savings goal not found"
                ));

        if (!goal.getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Savings goal not found"
            );
        }

        return goal;
    }

    // Find user using email
    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Convert Entity into Response DTO
    private SavingsGoalResponse convertToResponse(SavingsGoal goal) {

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetDate(),
                goal.getDescription(),
                goal.getCreatedAt(),
                calculateProgressPercentage(
                        goal.getTargetAmount(),
                        goal.getCurrentAmount()
                )
        );
    }

    // Calculate progress as a percentage, capped at 100%
    private double calculateProgressPercentage(
            BigDecimal targetAmount,
            BigDecimal currentAmount) {

        if (targetAmount == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {

            return 0.0;
        }

        BigDecimal progressPercentage = currentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);

        if (progressPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {

            return 100.0;
        }

        return progressPercentage.doubleValue();
    }
}
