package com.orderflow.payment_service.client;

import com.orderflow.payment_service.dto.StockItemRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    public boolean decreaseStock(
            List<StockItemRequest> items
    ) {

        String authorizationHeader =
                request.getHeader("Authorization");

        StockUpdateResponse response = restClient.patch()
                .uri("/api/products/stock/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authorizationHeader)
                .body(items)
                .retrieve()
                .body(StockUpdateResponse.class);

        return response != null && response.isSuccess();
    }

    private static class StockUpdateResponse {

        private boolean success;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}