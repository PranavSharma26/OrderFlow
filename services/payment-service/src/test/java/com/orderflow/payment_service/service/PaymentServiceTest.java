package com.orderflow.payment_service.service;

import com.orderflow.payment_service.client.OrderClient;
import com.orderflow.payment_service.client.ProductClient;
import com.orderflow.payment_service.dto.OrderItemResponse;
import com.orderflow.payment_service.dto.OrderResponse;
import com.orderflow.payment_service.dto.PaymentRequest;
import com.orderflow.payment_service.dto.PaymentResponse;
import com.orderflow.payment_service.entity.Payment;
import com.orderflow.payment_service.enums.PaymentStatus;
import com.orderflow.payment_service.exception.OrderNotFoundException;
import com.orderflow.payment_service.exception.PaymentAlreadyExistsException;
import com.orderflow.payment_service.repository.PaymentRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private PaymentService paymentService;


    // =========================================================
    // 1. Successful payment
    // =========================================================

    @Test
    void createPayment_shouldCreatePaymentSuccessfully() {

        // Arrange

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        OrderItemResponse item = mock(OrderItemResponse.class);

        when(item.getProductId())
                .thenReturn(10L);

        when(item.getQuantity())
                .thenReturn(2);

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(order.getItems())
                .thenReturn(List.of(item));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);

        when(productClient.decreaseStock(anyList()))
                .thenReturn(true);

        Payment savedPayment = new Payment();

        savedPayment.setId(100L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(new BigDecimal("1500.00"));
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        savedPayment.setTransactionId("TXN123");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);


        // Act

        PaymentResponse response =
                paymentService.createPayment(request);


        // Assert

        assertNotNull(response);

        assertEquals(100L, response.getId());

        assertEquals(1L, response.getOrderId());

        assertEquals(
                new BigDecimal("1500.00"),
                response.getAmount()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                "TXN123",
                response.getTransactionId()
        );

        verify(paymentRepository)
                .findByOrderId(1L);

        verify(orderClient)
                .getOrder(1L);

        verify(productClient)
                .decreaseStock(anyList());

        verify(paymentRepository)
                .save(any(Payment.class));

        verify(orderClient)
                .updateOrderStatus(1L, "CONFIRMED");
    }


    // =========================================================
    // 2. Duplicate payment
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenPaymentAlreadyExists() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        Payment existingPayment = new Payment();

        existingPayment.setId(100L);
        existingPayment.setOrderId(1L);

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(existingPayment));


        // Act & Assert

        assertThrows(
                PaymentAlreadyExistsException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository)
                .findByOrderId(1L);

        verify(orderClient, never())
                .getOrder(anyLong());

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 3. Order not found - OrderClient throws exception
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenOrderClientFails() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenThrow(
                        new RuntimeException("Order service unavailable")
                );


        // Act & Assert

        assertThrows(
                OrderNotFoundException.class,
                () -> paymentService.createPayment(request)
        );

        verify(orderClient)
                .getOrder(1L);

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 4. OrderClient returns null
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenOrderIsNull() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(null);


        // Act & Assert

        assertThrows(
                OrderNotFoundException.class,
                () -> paymentService.createPayment(request)
        );

        verify(orderClient)
                .getOrder(1L);

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 5. Payment amount does not match order total
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenPaymentAmountDoesNotMatchOrderTotal() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1000.00"));

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);


        // Act & Assert

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(request)
        );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Payment amount must exactly match order total"
                        )
        );

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 6. Order total is null
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenOrderTotalIsNull() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(null);

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);


        // Act & Assert

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(request)
        );

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 7. Payment amount is null
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenPaymentAmountIsNull() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(null);

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);


        // Act & Assert

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(request)
        );

        verify(productClient, never())
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 8. Insufficient stock
    // =========================================================

    @Test
    void createPayment_shouldThrowException_whenStockUpdateFails() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        OrderItemResponse item = mock(OrderItemResponse.class);

        when(item.getProductId())
                .thenReturn(10L);

        when(item.getQuantity())
                .thenReturn(2);

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(order.getItems())
                .thenReturn(List.of(item));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);

        when(productClient.decreaseStock(anyList()))
                .thenReturn(false);


        // Act & Assert

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                "Insufficient stock for one or more products",
                exception.getMessage()
        );

        verify(productClient)
                .decreaseStock(anyList());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(orderClient, never())
                .updateOrderStatus(anyLong(), anyString());
    }


    // =========================================================
    // 9. Correct stock items are sent to Product Service
    // =========================================================

    @Test
    void createPayment_shouldSendCorrectStockItems() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("2500.00"));

        OrderItemResponse item1 = mock(OrderItemResponse.class);

        when(item1.getProductId())
                .thenReturn(10L);

        when(item1.getQuantity())
                .thenReturn(2);

        OrderItemResponse item2 = mock(OrderItemResponse.class);

        when(item2.getProductId())
                .thenReturn(20L);

        when(item2.getQuantity())
                .thenReturn(3);

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("2500.00"));

        when(order.getItems())
                .thenReturn(List.of(item1, item2));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);

        when(productClient.decreaseStock(anyList()))
                .thenReturn(true);

        Payment savedPayment = new Payment();

        savedPayment.setId(100L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(new BigDecimal("2500.00"));
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        savedPayment.setTransactionId("TXN123");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);


        // Act

        paymentService.createPayment(request);


        // Assert

        verify(productClient).decreaseStock(
                argThat(stockItems ->
                        stockItems.size() == 2

                                && stockItems.get(0)
                                .getProductId()
                                .equals(10L)

                                && stockItems.get(0)
                                .getQuantity() == 2

                                && stockItems.get(1)
                                .getProductId()
                                .equals(20L)

                                && stockItems.get(1)
                                .getQuantity() == 3
                )
        );
    }


    // =========================================================
    // 10. Payment is saved with SUCCESS status
    // =========================================================

    @Test
    void createPayment_shouldSavePaymentWithSuccessStatus() {

        // Arrange

        PaymentRequest request = new PaymentRequest();

        request.setOrderId(1L);
        request.setAmount(new BigDecimal("1500.00"));

        OrderItemResponse item = mock(OrderItemResponse.class);

        when(item.getProductId())
                .thenReturn(10L);

        when(item.getQuantity())
                .thenReturn(2);

        OrderResponse order = mock(OrderResponse.class);

        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(order.getItems())
                .thenReturn(List.of(item));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(orderClient.getOrder(1L))
                .thenReturn(order);

        when(productClient.decreaseStock(anyList()))
                .thenReturn(true);

        Payment savedPayment = new Payment();

        savedPayment.setId(100L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(new BigDecimal("1500.00"));
        savedPayment.setStatus(PaymentStatus.SUCCESS);
        savedPayment.setTransactionId("TXN123");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);


        // Act

        paymentService.createPayment(request);


        // Assert

        verify(paymentRepository).save(
                argThat(payment ->
                        payment.getOrderId().equals(1L)

                                && payment.getAmount()
                                .compareTo(
                                        new BigDecimal("1500.00")
                                ) == 0

                                && payment.getStatus()
                                == PaymentStatus.SUCCESS

                                && payment.getTransactionId() != null
                )
        );
    }
}