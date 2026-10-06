package com.markethub.app.controller;

import com.markethub.app.model.Order;
import com.markethub.app.security.AccessGuard;
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
    private final AccessGuard accessGuard;

    public OrderController(OrderService orderService, UserDetailsServiceImpl userDetailsServiceImpl,
                           AccessGuard accessGuard) {
        this.orderService = orderService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/user/{userId}")
    public String getOrdersByOwnerUserId(@PathVariable("userId") Long userId, Model model) {
        accessGuard.requireSelfOrAdmin(userId);
        List<Order> orders = orderService.getOrdersByOwnerUserId(userId);
        double totalSpent = orders.stream().mapToDouble(Order::getPrice).sum();
        model.addAttribute("orders", orders);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/buyer/order/orderPage";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        accessGuard.requireOwnerOrAdmin(order.getOwner());
        if ("Pending".equals(order.getOrderStatus())) {
            order.setOrderStatus("Cancelled");
            orderService.saveOrder(order);
        }
        return "redirect:/orders/user/" + order.getOwner().getUserId();
    }

    @PostMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        accessGuard.requireOwnerOrAdmin(order.getOwner());
        orderService.deleteOrderById(orderId);
        return "redirect:/orders/user/" + order.getOwner().getUserId();
    }
}
