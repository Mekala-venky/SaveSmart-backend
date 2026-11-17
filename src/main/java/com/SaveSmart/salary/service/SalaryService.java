package com.SaveSmart.salary.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.salary.dto.SalaryRequest;
import com.SaveSmart.salary.dto.SalaryResponse;
import com.SaveSmart.salary.entity.Salary;
import com.SaveSmart.salary.repository.SalaryRepository;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;

    public SalaryService(
            SalaryRepository salaryRepository,
            UserRepository userRepository) {
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
    }

    public SalaryResponse addSalary(String email, SalaryRequest request) {

        User user = getUser(email);

        if (salaryRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Salary details already exist. Please update your salary instead."
            );
        }

        Salary salary = new Salary();
        salary.setMonthlySalary(request.getMonthlySalary());
        salary.setSalarySource(request.getSalarySource());
        salary.setSalaryReceivedDate(request.getSalaryReceivedDate());
        salary.setUser(user);

        Salary savedSalary = salaryRepository.save(salary);

        return convertToResponse(savedSalary);
    }

    public SalaryResponse getSalary(String email) {

        User user = getUser(email);

        Salary salary = salaryRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Salary details not found."
                ));

        return convertToResponse(salary);
    }

    public SalaryResponse updateSalary(String email, SalaryRequest request) {

        User user = getUser(email);

        Salary salary = salaryRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Salary details not found. Please add your salary first."
                ));

        salary.setMonthlySalary(request.getMonthlySalary());
        salary.setSalarySource(request.getSalarySource());
        salary.setSalaryReceivedDate(request.getSalaryReceivedDate());

        Salary updatedSalary = salaryRepository.save(salary);

        return convertToResponse(updatedSalary);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));
    }

    private SalaryResponse convertToResponse(Salary salary) {
        return new SalaryResponse(
                salary.getId(),
                salary.getMonthlySalary(),
                salary.getSalarySource(),
                salary.getSalaryReceivedDate(),
                salary.getCreatedAt()
        );
    }
}