package com.SaveSmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.SaveSmart.auth.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter)
                        throws Exception {

                http
                                .cors(Customizer.withDefaults())

                                .csrf(csrf -> csrf.disable())

                                .formLogin(form -> form.disable())

                                .httpBasic(basic -> basic.disable())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response,
                                                                authException) -> writeJsonError(
                                                                                response,
                                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized",
                                                                                "Authentication is required to access this resource."))
                                                .accessDeniedHandler((request, response,
                                                                accessDeniedException) -> writeJsonError(
                                                                                response,
                                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                                "Forbidden",
                                                                                "You do not have permission to access this resource.")))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/register",
                                                                "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers("/error").permitAll()

                                                // Only reviewers can read the queue and
                                                // approve or reject withdrawals.
                                                .requestMatchers(
                                                                "/api/savings/withdrawal-requests/review",
                                                                "/api/savings/withdrawal-requests/review/**")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        private static void writeJsonError(
                        HttpServletResponse response,
                        int status,
                        String error,
                        String message) throws java.io.IOException {

                response.setStatus(status);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                                "{\"status\":" + status
                                                + ",\"error\":\"" + error
                                                + "\",\"message\":\"" + message + "\"}");
        }
}