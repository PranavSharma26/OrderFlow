package com.orderflow.auth_service.controller;

import com.orderflow.auth_service.dto.RegisterRequest;
import com.orderflow.auth_service.response.ApiResponse;
import com.orderflow.auth_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse(
                LocalDateTime.now(),
                "User registered successfully",
                200,
                true
        ));
    }
}
