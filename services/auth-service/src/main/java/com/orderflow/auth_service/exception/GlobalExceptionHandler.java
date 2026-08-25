package com.orderflow.auth_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception){
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                exception.getStatus(),
                exception.getMessage()
        );
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

}
