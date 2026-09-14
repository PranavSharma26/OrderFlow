package com.orderflow.order_service.controller;

import com.orderflow.order_service.dto.OrderItemResponse;
import com.orderflow.order_service.dto.OrderRequest;
import com.orderflow.order_service.dto.OrderResponse;
import com.orderflow.order_service.dto.OrderStatusUpdateRequest;
import com.orderflow.order_service.entity.Order;
import com.orderflow.order_service.response.ApiResponse;
import com.orderflow.order_service.service.OrderService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request
    ) {

        Long userId = (Long) authentication.getPrincipal();

        Order order = orderService.createOrder(userId, request);

        OrderResponse orderResponse = toOrderResponse(order);

        ApiResponse response = new ApiResponse(
                LocalDateTime.now(),
                "Order created successfully",
                200,
                true,
                orderResponse
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getMyOrders(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        List<Order> orders =
                orderService.getOrdersByUserId(userId);

        List<OrderResponse> orderResponses = orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        ApiResponse response = new ApiResponse(
                LocalDateTime.now(),
                "Orders fetched successfully",
                200,
                true,
                orderResponses
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        Order order =
                orderService.getOrderById(orderId, userId);

        if (order == null) {

            ApiResponse response = new ApiResponse(
                    LocalDateTime.now(),
                    "Order not found",
                    404,
                    false,
                    null
            );

            return ResponseEntity
                    .status(404)
                    .body(response);
        }

        ApiResponse response = new ApiResponse(
                LocalDateTime.now(),
                "Order fetched successfully",
                200,
                true,
                toOrderResponse(order)
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            Authentication authentication,
            @RequestBody OrderStatusUpdateRequest request
    ) {

        Long userId = (Long) authentication.getPrincipal();

        Order order = orderService.updateOrderStatus(
                orderId,
                userId,
                request.getStatus()
        );

        if (order == null) {

            ApiResponse response = new ApiResponse(
                    LocalDateTime.now(),
                    "Invalid order status transition or order not found",
                    404,
                    false,
                    null
            );

            return ResponseEntity
                    .status(404)
                    .body(response);
        }

        ApiResponse response = new ApiResponse(
                LocalDateTime.now(),
                "Order status updated successfully",
                200,
                true,
                toOrderResponse(order)
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        Order order = orderService.cancelOrder(
                orderId,
                userId
        );

        if (order == null) {

            ApiResponse response = new ApiResponse(
                    LocalDateTime.now(),
                    "Order cannot be cancelled or order not found",
                    404,
                    false,
                    null
            );

            return ResponseEntity
                    .status(404)
                    .body(response);
        }

        ApiResponse response = new ApiResponse(
                LocalDateTime.now(),
                "Order cancelled successfully",
                200,
                true,
                toOrderResponse(order)
        );

        return ResponseEntity.ok(response);
    }

    private OrderResponse toOrderResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }
}