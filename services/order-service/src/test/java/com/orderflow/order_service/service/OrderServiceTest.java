package com.orderflow.order_service.service;

import com.orderflow.order_service.client.ProductClient;
import com.orderflow.order_service.client.ProductResponse;
import com.orderflow.order_service.dto.OrderItemRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

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


    // =========================================================
    // createOrder()
    // =========================================================

    @Test
    void createOrder_shouldCreatePendingOrder() {

        // Arrange

        OrderItemRequest itemRequest =
                mock(OrderItemRequest.class);

        when(itemRequest.getProductId())
                .thenReturn(1L);

        when(itemRequest.getQuantity())
                .thenReturn(2);

        OrderRequest request = new OrderRequest();

        request.setItems(
                List.of(itemRequest)
        );

        ProductResponse product =
                mock(ProductResponse.class);

        when(product.getId())
                .thenReturn(1L);

        when(product.getPrice())
                .thenReturn(new BigDecimal("250.00"));

        when(product.getStockQuantity())
                .thenReturn(10);

        when(productClient.getProduct(1L))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);


        // Act

        Order result =
                orderService.createOrder(
                        userId,
                        request
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                userId,
                result.getUserId()
        );

        assertEquals(
                new BigDecimal("500.00"),
                result.getTotalAmount()
        );

        assertEquals(
                OrderStatus.PENDING,
                result.getStatus()
        );

        verify(productClient)
                .getProduct(1L);

        verify(orderRepository)
                .save(any(Order.class));
    }


    @Test
    void createOrder_shouldCalculateTotalForMultipleItems() {

        // Arrange

        OrderItemRequest item1 =
                mock(OrderItemRequest.class);

        when(item1.getProductId())
                .thenReturn(1L);

        when(item1.getQuantity())
                .thenReturn(2);


        OrderItemRequest item2 =
                mock(OrderItemRequest.class);

        when(item2.getProductId())
                .thenReturn(2L);

        when(item2.getQuantity())
                .thenReturn(3);


        OrderRequest request = new OrderRequest();

        request.setItems(
                List.of(item1, item2)
        );


        ProductResponse product1 =
                mock(ProductResponse.class);

        when(product1.getId())
                .thenReturn(1L);

        when(product1.getPrice())
                .thenReturn(new BigDecimal("100.00"));

        when(product1.getStockQuantity())
                .thenReturn(10);


        ProductResponse product2 =
                mock(ProductResponse.class);

        when(product2.getId())
                .thenReturn(2L);

        when(product2.getPrice())
                .thenReturn(new BigDecimal("200.00"));

        when(product2.getStockQuantity())
                .thenReturn(10);


        when(productClient.getProduct(1L))
                .thenReturn(product1);

        when(productClient.getProduct(2L))
                .thenReturn(product2);


        Order savedOrder = new Order(
                userId,
                new BigDecimal("800.00"),
                OrderStatus.PENDING
        );

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);


        // Act

        Order result =
                orderService.createOrder(
                        userId,
                        request
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                new BigDecimal("800.00"),
                result.getTotalAmount()
        );

        assertEquals(
                OrderStatus.PENDING,
                result.getStatus()
        );

        verify(productClient)
                .getProduct(1L);

        verify(productClient)
                .getProduct(2L);

        verify(orderRepository)
                .save(any(Order.class));
    }


    @Test
    void createOrder_shouldThrowException_whenProductNotFound() {

        // Arrange

        OrderItemRequest itemRequest =
                mock(OrderItemRequest.class);

        when(itemRequest.getProductId())
                .thenReturn(999L);

        OrderRequest request = new OrderRequest();

        request.setItems(
                List.of(itemRequest)
        );

        when(productClient.getProduct(999L))
                .thenReturn(null);


        // Act & Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(
                        userId,
                        request
                )
        );

        assertEquals(
                "Product not found: 999",
                exception.getMessage()
        );

        verify(productClient)
                .getProduct(999L);

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void createOrder_shouldThrowException_whenStockIsInsufficient() {
        OrderItemRequest itemRequest = mock(OrderItemRequest.class);

        when(itemRequest.getProductId()).thenReturn(1L);
        when(itemRequest.getQuantity()).thenReturn(10);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        ProductResponse product = mock(ProductResponse.class);

        when(product.getStockQuantity()).thenReturn(5);

        when(productClient.getProduct(1L)).thenReturn(product);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(userId, request)
        );

        assertEquals(
                "Insufficient stock for product: 1",
                exception.getMessage()
        );

        verify(productClient).getProduct(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }


    // =========================================================
    // getOrdersByUserId()
    // =========================================================

    @Test
    void getOrdersByUserId_shouldReturnUserOrders() {

        // Arrange

        List<Order> orders = List.of(order);

        when(orderRepository.findByUserId(userId))
                .thenReturn(orders);


        // Act

        List<Order> result =
                orderService.getOrdersByUserId(userId);


        // Assert

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                userId,
                result.get(0).getUserId()
        );

        verify(orderRepository)
                .findByUserId(userId);
    }


    // =========================================================
    // getOrderById()
    // =========================================================

    @Test
    void getOrderById_shouldReturnOrderWhenFound() {

        // Arrange

        Long orderId = 1L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));


        // Act

        Order result =
                orderService.getOrderById(
                        orderId,
                        userId
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                userId,
                result.getUserId()
        );

        verify(orderRepository)
                .findByIdAndUserId(
                        orderId,
                        userId
                );
    }


    @Test
    void getOrderById_shouldReturnNullWhenNotFound() {

        // Arrange

        Long orderId = 999L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.empty());


        // Act

        Order result =
                orderService.getOrderById(
                        orderId,
                        userId
                );


        // Assert

        assertNull(result);

        verify(orderRepository)
                .findByIdAndUserId(
                        orderId,
                        userId
                );
    }


    // =========================================================
    // updateOrderStatus()
    // =========================================================

    @Test
    void updateOrderStatus_shouldAllowPendingToConfirmed() {

        // Arrange

        Long orderId = 1L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.CONFIRMED
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.CONFIRMED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void updateOrderStatus_shouldAllowConfirmedToProcessing() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.PROCESSING
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.PROCESSING,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void updateOrderStatus_shouldAllowProcessingToShipped() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.PROCESSING
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.SHIPPED
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.SHIPPED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void updateOrderStatus_shouldAllowShippedToDelivered() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.SHIPPED
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.DELIVERED
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.DELIVERED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void updateOrderStatus_shouldRejectInvalidTransition() {

        // Arrange

        Long orderId = 1L;

        // Current status = PENDING
        // Trying PENDING → SHIPPED

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.SHIPPED
                );


        // Assert

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void updateOrderStatus_shouldReturnNullWhenOrderNotFound() {

        // Arrange

        Long orderId = 999L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.empty());


        // Act

        Order result =
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        OrderStatus.CONFIRMED
                );


        // Assert

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    // =========================================================
    // cancelOrder()
    // =========================================================

    @Test
    void cancelOrder_shouldAllowPendingOrder() {

        // Arrange

        Long orderId = 1L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void cancelOrder_shouldAllowConfirmedOrder() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void cancelOrder_shouldAllowProcessingOrder() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.PROCESSING
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNotNull(result);

        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

        verify(orderRepository)
                .save(order);
    }


    @Test
    void cancelOrder_shouldRejectShippedOrder() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.SHIPPED
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void cancelOrder_shouldRejectDeliveredOrder() {

        // Arrange

        Long orderId = 1L;

        order.setStatus(
                OrderStatus.DELIVERED
        );

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.of(order));


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void cancelOrder_shouldReturnNullWhenOrderNotFound() {

        // Arrange

        Long orderId = 999L;

        when(
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                )
        ).thenReturn(Optional.empty());


        // Act

        Order result =
                orderService.cancelOrder(
                        orderId,
                        userId
                );


        // Assert

        assertNull(result);

        verify(orderRepository, never())
                .save(any(Order.class));
    }
}