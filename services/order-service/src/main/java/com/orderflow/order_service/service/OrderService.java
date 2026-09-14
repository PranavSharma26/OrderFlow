package com.orderflow.order_service.service;

import com.orderflow.order_service.client.ProductClient;
import com.orderflow.order_service.client.ProductResponse;
import com.orderflow.order_service.dto.OrderItemRequest;
import com.orderflow.order_service.dto.OrderRequest;
import com.orderflow.order_service.entity.Order;
import com.orderflow.order_service.entity.OrderItem;
import com.orderflow.order_service.enums.OrderStatus;
import com.orderflow.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient
    ) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    public Order createOrder(Long userId, OrderRequest request) {

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = new Order(
                userId,
                totalAmount,
                OrderStatus.PENDING
        );

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product =
                    productClient.getProduct(itemRequest.getProductId());

            if (product == null) {
                throw new RuntimeException(
                        "Product not found: " + itemRequest.getProductId()
                );
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + itemRequest.getProductId()
                );
            }

            BigDecimal itemTotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            );

            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    itemRequest.getQuantity(),
                    product.getPrice()
            );

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);

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