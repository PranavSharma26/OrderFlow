package com.orderflow.order_service.service;

import com.orderflow.order_service.dto.OrderRequest;
import com.orderflow.order_service.entity.Order;
import com.orderflow.order_service.enums.OrderStatus;
import com.orderflow.order_service.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Long userId;
    private Order order;

    @BeforeEach
    void setUp() {

        userId = 10L;

        order = new Order(
                userId,
                new BigDecimal("500.00"),
                OrderStatus.PENDING
        );
    }

    // ---------------------------------------------------------
    // createOrder()
    // ---------------------------------------------------------

    @Test
    void createOrder_shouldCreatePendingOrder() {

        OrderRequest request = new OrderRequest();
        request.setTotalAmount(new BigDecimal("500.00"));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        Order result =
                orderService.createOrder(userId, request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(
                new BigDecimal("500.00"),
                result.getTotalAmount()
        );
        assertEquals(
                OrderStatus.PENDING,
                result.getStatus()
        );

        verify(orderRepository, times(1))
                .save(any(Order.class));
    }

    // ---------------------------------------------------------
    // getOrdersByUserId()
    // ---------------------------------------------------------

    @Test
    void getOrdersByUserId_shouldReturnUserOrders() {

        List<Order> orders = List.of(order);

        when(orderRepository.findByUserId(userId))
                .thenReturn(orders);

        List<Order> result =
                orderService.getOrdersByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());

        verify(orderRepository, times(1))
                .findByUserId(userId);
    }

    // ---------------------------------------------------------
    // getOrderById()
    // ---------------------------------------------------------

    @Test
    void getOrderById_shouldReturnOrderWhenFound() {

        Long orderId = 1L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        Order result =
                orderService.getOrderById(orderId, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(orderRepository, times(1))
                .findByIdAndUserId(orderId, userId);
    }

    @Test
    void getOrderById_shouldReturnNullWhenNotFound() {

        Long orderId = 999L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.empty());

        Order result =
                orderService.getOrderById(orderId, userId);

        assertNull(result);

        verify(orderRepository, times(1))
                .findByIdAndUserId(orderId, userId);
    }

    // ---------------------------------------------------------
    // updateOrderStatus()
    // ---------------------------------------------------------

    @Test
    void updateOrderStatus_shouldAllowPendingToConfirmed() {

        Long orderId = 1L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.CONFIRMED
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.CONFIRMED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldAllowConfirmedToProcessing() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.PROCESSING
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.PROCESSING,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldAllowProcessingToShipped() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.SHIPPED
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.SHIPPED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldAllowShippedToDelivered() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.DELIVERED
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.DELIVERED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_shouldRejectInvalidTransition() {

        Long orderId = 1L;

        // Current status = PENDING
        // Trying PENDING → SHIPPED

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.SHIPPED
                );

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldReturnNullWhenOrderNotFound() {

        Long orderId = 999L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.empty());

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.CONFIRMED
                );

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    // ---------------------------------------------------------
    // cancelOrder()
    // ---------------------------------------------------------

    @Test
    void cancelOrder_shouldAllowPendingOrder() {

        Long orderId = 1L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldAllowConfirmedOrder() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldAllowProcessingOrder() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNotNull(result);
        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldRejectShippedOrder() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldRejectDeliveredOrder() {

        Long orderId = 1L;

        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void cancelOrder_shouldReturnNullWhenOrderNotFound() {

        Long orderId = 999L;

        when(orderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.empty());

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }
}