package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Order;
import com.markethub.app.repository.OrderRepository;
import com.markethub.app.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersPage(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        log.info("Saving order status={}", order.getOrderStatus());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrderById(Long orderId) {
        log.info("Deleting order id={}", orderId);
        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByOwnerUserId(Long userId) {
        return orderRepository.getOrderByOwnerUserId(userId);
    }
}
