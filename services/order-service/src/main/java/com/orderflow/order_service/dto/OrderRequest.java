package com.orderflow.order_service.dto;

import java.math.BigDecimal;

public class OrderRequest {

    private BigDecimal totalAmount;

    public OrderRequest() {
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}