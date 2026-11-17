package com.SaveSmart.dashboard.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SaveSmart.dashboard.dto.DashboardResponse;
import com.SaveSmart.dashboard.dto.MonthlyAnalysisResponse;
import com.SaveSmart.dashboard.dto.YearlyAnalysisResponse;
import com.SaveSmart.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Dashboard summary (defaults to the current month)
    @GetMapping
    public DashboardResponse getDashboard(
            Authentication authentication,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        String email = authentication.getName();

        return dashboardService.getDashboard(email, month, year);
    }

    // Monthly analysis
    @GetMapping("/monthly")
    public MonthlyAnalysisResponse getMonthlyAnalysis(
            Authentication authentication,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        String email = authentication.getName();

        return dashboardService.getMonthlyAnalysis(
                email, month, year
        );
    }

    // Yearly analysis with a month-by-month breakdown
    @GetMapping("/yearly")
    public YearlyAnalysisResponse getYearlyAnalysis(
            Authentication authentication,
            @RequestParam(required = false) Integer year) {

        String email = authentication.getName();

        return dashboardService.getYearlyAnalysis(email, year);
    }
}
