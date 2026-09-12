package com.orderflow.order_service.service;

import com.orderflow.order_service.dto.OrderRequest;
import com.orderflow.order_service.entity.Order;
import com.orderflow.order_service.enums.OrderStatus;
import com.orderflow.order_service.repository.OrderRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Long userId, OrderRequest request) {

        Order order = new Order(
                userId,
                request.getTotalAmount(),
                OrderStatus.PENDING
        );

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long orderId, Long userId) {

        return orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);
    }

    public Order updateOrderStatus(
            Long orderId,
            Long userId,
            OrderStatus newStatus
    ) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);

        if (order == null) {
            return null;
        }

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.PENDING
                && newStatus == OrderStatus.CONFIRMED) {

            order.setStatus(newStatus);

        } else if (currentStatus == OrderStatus.CONFIRMED
                && newStatus == OrderStatus.PROCESSING) {

            order.setStatus(newStatus);

        } else if (currentStatus == OrderStatus.PROCESSING
                && newStatus == OrderStatus.SHIPPED) {

            order.setStatus(newStatus);

        } else if (currentStatus == OrderStatus.SHIPPED
                && newStatus == OrderStatus.DELIVERED) {

            order.setStatus(newStatus);

        } else {
            return null;
        }

        return orderRepository.save(order);
    }

    public Order cancelOrder(
            Long orderId,
            Long userId
    ) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);

        if (order == null) {
            return null;
        }

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.PENDING
                || currentStatus == OrderStatus.CONFIRMED
                || currentStatus == OrderStatus.PROCESSING) {

            order.setStatus(OrderStatus.CANCELLED);

            return orderRepository.save(order);
        }

        return null;
    }

}