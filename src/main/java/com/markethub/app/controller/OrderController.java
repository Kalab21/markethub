package com.markethub.app.controller;

import com.markethub.app.model.Order;
import com.markethub.app.service.OrderService;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value="/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public OrderController(OrderService orderService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.orderService = orderService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/user/{userId}")
    public String getOrdersByOwnerUserId(@PathVariable("userId") Long userId, Model model) {
        List<Order> orders = orderService.getOrdersByOwnerUserId(userId);
        double totalSpent = orders.stream().mapToDouble(Order::getPrice).sum();
        model.addAttribute("orders", orders);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/buyer/order/orderPage";
    }

    @GetMapping("/cancel/{orderId}/user/{userId}")
    public String cancelOrder(@PathVariable Long orderId, @PathVariable Long userId) {
        Order order = orderService.getOrderById(orderId);
        if ("Pending".equals(order.getOrderStatus())) {
            order.setOrderStatus("Cancelled");
            orderService.saveOrder(order);
        }
        return "redirect:/orders/user/" + userId;
    }

    @GetMapping("/delete/{orderId}/user/{userId}")
    public String deleteOrder(@PathVariable Long orderId, @PathVariable Long userId) {
        orderService.deleteOrderById(orderId);
        return "redirect:/orders/user/" + userId;
    }
}
