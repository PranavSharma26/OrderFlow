package com.orderflow.payment_service.client;

import com.orderflow.payment_service.dto.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OrderClient {

    private final RestClient restClient;
    private final HttpServletRequest request;

    public OrderClient(
            RestClient.Builder builder,
            @Value("${order.service.url}") String orderServiceUrl,
            HttpServletRequest request
    ) {
        this.restClient = builder
                .baseUrl(orderServiceUrl)
                .build();

        this.request = request;
    }

    public OrderResponse getOrder(Long orderId) {

        String authorizationHeader =
                request.getHeader("Authorization");

        return restClient.get()
                .uri("/api/v1/orders/{orderId}", orderId)
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(OrderResponseWrapper.class)
                .getData();
    }

    public void updateOrderStatus(Long orderId, String status) {

        String authorizationHeader =
                request.getHeader("Authorization");

        restClient.patch()
                .uri("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authorizationHeader)
                .body(Map.of("status", status))
                .retrieve()
                .toBodilessEntity();
    }

    private static class OrderResponseWrapper {

        private OrderResponse data;

        public OrderResponse getData() {
            return data;
        }

        public void setData(OrderResponse data) {
            this.data = data;
        }
    }
}