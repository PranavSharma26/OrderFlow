package com.orderflow.order_service.dto;

import com.orderflow.order_service.enums.OrderStatus;

public class OrderStatusUpdateRequest {

    private OrderStatus status;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}