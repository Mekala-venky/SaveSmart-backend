package com.SaveSmart.insights.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SaveSmart.insights.dto.InsightsResponse;
import com.SaveSmart.insights.service.InsightsService;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    // Personalised savings insights and financial health score
    @GetMapping
    public InsightsResponse getInsights(
            Authentication authentication) {

        String email = authentication.getName();

        return insightsService.getInsights(email);
    }
}
