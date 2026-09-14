package com.orderflow.payment_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(
            RestClient.Builder builder,
            @Value("${order.service.url}") String orderServiceUrl
    ) {
        this.restClient = builder
                .baseUrl(orderServiceUrl)
                .build();
    }

    public boolean orderExists(Long orderId) {

        try {
            restClient.get()
                    .uri("/api/orders/{orderId}", orderId)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}