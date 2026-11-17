
package com.SaveSmart.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.SaveSmart.user.dto.RegisterRequest;
import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {

        // Check whether email already exists
        if (userRepository.existsByEmail(
        request.getEmail().trim().toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Create a new user
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("USER");

        // Encrypt password before storing
        user.setPasswordHash(
            passwordEncoder.encode(request.getPassword())
        );

        // Save user into MySQL
        return userRepository.save(user);
    }
}
