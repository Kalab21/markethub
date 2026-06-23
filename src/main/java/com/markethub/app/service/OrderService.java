package com.markethub.app.service;

import com.markethub.app.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    Page<Order> getOrdersPage(int page, int size);
    Order getOrderById(Long orderId);
    Order saveOrder(Order order);
    void deleteOrderById(Long orderId);
    List<Order> getOrdersByOwnerUserId(Long userId);
}
