package com.orderflow.payment_service.service;

import com.orderflow.payment_service.client.OrderClient;
import com.orderflow.payment_service.client.ProductClient;
import com.orderflow.payment_service.dto.OrderItemResponse;
import com.orderflow.payment_service.dto.OrderResponse;
import com.orderflow.payment_service.dto.PaymentRequest;
import com.orderflow.payment_service.dto.PaymentResponse;
import com.orderflow.payment_service.dto.StockItemRequest;
import com.orderflow.payment_service.entity.Payment;
import com.orderflow.payment_service.enums.PaymentStatus;
import com.orderflow.payment_service.exception.OrderNotFoundException;
import com.orderflow.payment_service.exception.PaymentAlreadyExistsException;
import com.orderflow.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final ProductClient productClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderClient orderClient,
            ProductClient productClient
    ) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.productClient = productClient;
    }

    public PaymentResponse createPayment(PaymentRequest request) {

        // 1. Prevent duplicate payment
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new PaymentAlreadyExistsException(
                    "Payment already exists for order: "
                            + request.getOrderId()
            );
        }

        // 2. Fetch the actual order
        OrderResponse order;

        try {
            order = orderClient.getOrder(request.getOrderId());
        } catch (Exception e) {
            throw new OrderNotFoundException(
                    "Order not found: " + request.getOrderId()
            );
        }

        if (order == null) {
            throw new OrderNotFoundException(
                    "Order not found: " + request.getOrderId()
            );
        }

        // 3. Payment amount MUST exactly match order total
        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal paymentAmount = request.getAmount();

        if (orderTotal == null ||
                paymentAmount == null ||
                paymentAmount.compareTo(orderTotal) != 0) {

            throw new IllegalArgumentException(
                    "Payment amount must exactly match order total. "
                            + "Order total: " + orderTotal
                            + ", payment amount: " + paymentAmount
            );
        }

        // 4. Build stock request from the order
        List<StockItemRequest> stockItems = order.getItems()
                .stream()
                .map(item -> new StockItemRequest(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        // 5. Decrease stock atomically
        boolean stockUpdated =
                productClient.decreaseStock(stockItems);

        if (!stockUpdated) {

            throw new IllegalStateException(
                    "Insufficient stock for one or more products"
            );
        }

        // 6. Simulated payment processing
        Payment payment = new Payment();

        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        // 7. Payment succeeded → confirm order
        orderClient.updateOrderStatus(
                savedPayment.getOrderId(),
                "CONFIRMED"
        );

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