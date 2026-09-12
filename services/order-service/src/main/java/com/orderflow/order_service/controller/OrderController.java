package com.orderflow.order_service.controller;

import com.orderflow.order_service.dto.OrderRequest;
import com.orderflow.order_service.entity.Order;
import com.orderflow.order_service.service.OrderService;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Order order = orderService.createOrder(userId, request);

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        List<Order> orders =
                orderService.getOrdersByUserId(userId);

        return ResponseEntity.ok(orders);
    }

}