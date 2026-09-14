package com.orderflow.payment_service.service;

import com.orderflow.payment_service.client.OrderClient;
import com.orderflow.payment_service.dto.PaymentRequest;
import com.orderflow.payment_service.dto.PaymentResponse;
import com.orderflow.payment_service.entity.Payment;
import com.orderflow.payment_service.enums.PaymentStatus;
import com.orderflow.payment_service.exception.OrderNotFoundException;
import com.orderflow.payment_service.exception.PaymentAlreadyExistsException;
import com.orderflow.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderClient orderClient
    ) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    public PaymentResponse createPayment(PaymentRequest request) {

        // Verify that the order exists
        if (!orderClient.orderExists(request.getOrderId())) {
            throw new OrderNotFoundException(
                    "Order not found: " + request.getOrderId()
            );
        }

        // Prevent duplicate payment for the same order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new PaymentAlreadyExistsException(
                    "Payment already exists for order: " + request.getOrderId()
            );
        }

        Payment payment = new Payment();

        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        // Simulated payment processing
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    private PaymentResponse mapToResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }
}