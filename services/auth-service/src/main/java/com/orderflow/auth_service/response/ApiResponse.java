package com.orderflow.auth_service.response;

import java.time.LocalDateTime;

public class ApiResponse {

    private LocalDateTime timestamp;
    private String message;
    private int status;
    private boolean success;

    public ApiResponse(
            LocalDateTime timestamp,
            String message,
            int status,
            boolean success
    ) {
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
        this.success = success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }
}