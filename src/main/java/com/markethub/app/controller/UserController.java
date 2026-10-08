package com.markethub.app.controller;

import com.markethub.app.service.UserService;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/onlinemarket/secured/services/users")
public class UserController {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public UserController(UserService userService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userService = userService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/sellers")
    public String getAllSellers(Model model) {
        model.addAttribute("sellers", userService.getSellers());
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/admin/usrmgmt/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{sellerId}/approve")
    public String approveSeller(@PathVariable("sellerId") long sellerId) {
        userService.approveSeller(sellerId);
        return "redirect:/onlinemarket/secured/services/users/sellers";
    }
}
