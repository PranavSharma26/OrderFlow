package com.orderflow.payment_service.controller;

import com.orderflow.payment_service.dto.PaymentRequest;
import com.orderflow.payment_service.dto.PaymentResponse;
import com.orderflow.payment_service.response.ApiResponse;
import com.orderflow.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request);

        ApiResponse apiResponse = new ApiResponse(
                LocalDateTime.now(),
                "Payment created successfully",
                HttpStatus.CREATED.value(),
                true,
                response
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
}