package com.SaveSmart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.SaveSmart.user.entity.User;
import com.SaveSmart.user.repository.UserRepository;

/**
 * Creates a reviewer account at startup so withdrawal requests can be
 * approved. Credentials can be overridden with environment variables:
 *
 * SAVESMART_REVIEWER_EMAIL
 * SAVESMART_REVIEWER_PASSWORD
 * SAVESMART_REVIEWER_NAME
 * SAVESMART_REVIEWER_ENABLED
 *
 * The defaults exist only to make the demo work out of the box. Change them,
 * or disable the reviewer account, before exposing the app publicly.
 */
@Component
public class ReviewerBootstrap implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewerBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${savesmart.reviewer.enabled:true}")
    private boolean enabled;

    @Value("${savesmart.reviewer.email:reviewer@savesmart.app}")
    private String reviewerEmail;

    @Value("${savesmart.reviewer.password:Reviewer123}")
    private String reviewerPassword;

    @Value("${savesmart.reviewer.full-name:SaveSmart Reviewer}")
    private String reviewerName;

    public ReviewerBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (!enabled) {
            log.info("Withdrawal reviewer bootstrap is disabled");
            return;
        }

        String email = reviewerEmail.trim().toLowerCase();

        User reviewer = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User created = new User();

                    created.setFullName(reviewerName);
                    created.setEmail(email);
                    created.setPasswordHash(
                            passwordEncoder.encode(reviewerPassword)
                    );

                    return created;
                });

        if (!"ADMIN".equals(reviewer.getRole())) {
            reviewer.setRole("ADMIN");
        }

        userRepository.save(reviewer);

        log.info("Withdrawal reviewer account ready: {}", email);
    }
}
