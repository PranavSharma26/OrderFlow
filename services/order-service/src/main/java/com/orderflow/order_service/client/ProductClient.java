package com.orderflow.order_service.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient restClient;
    private final HttpServletRequest request;

    public ProductClient(
            RestClient.Builder builder,
            @Value("${product.service.url}") String productServiceUrl,
            HttpServletRequest request
    ) {
        this.restClient = builder
                .baseUrl(productServiceUrl)
                .build();

        this.request = request;
    }

    public ProductResponse getProduct(Long productId) {

        String authorizationHeader =
                request.getHeader("Authorization");

        return restClient.get()
                .uri("/api/products/{productId}", productId)
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(ProductResponse.class);
    }
}