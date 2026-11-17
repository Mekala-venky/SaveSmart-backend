package com.SaveSmart.insights.dto;

import java.util.List;

public class InsightsResponse {

    private int healthScore;
    private String rating;
    private double savingsRate;
    private List<InsightResponse> insights;

    public InsightsResponse(
            int healthScore,
            String rating,
            double savingsRate,
            List<InsightResponse> insights) {

        this.healthScore = healthScore;
        this.rating = rating;
        this.savingsRate = savingsRate;
        this.insights = insights;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public String getRating() {
        return rating;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public List<InsightResponse> getInsights() {
        return insights;
    }
}
