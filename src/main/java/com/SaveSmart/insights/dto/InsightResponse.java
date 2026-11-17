package com.SaveSmart.insights.dto;

public class InsightResponse {

    private String severity;
    private String title;
    private String message;

    public InsightResponse(
            String severity,
            String title,
            String message) {

        this.severity = severity;
        this.title = title;
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
