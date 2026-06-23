package com.markethub.app.controller;

import com.markethub.app.DTO.SignUp;
import com.markethub.app.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/onlinemarket/public/signup")
public class SignUpController {

    private final UserService userService;

    public SignUpController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showSignUpForm(Model model) {
        model.addAttribute("signUp", new SignUp());
        return "public/signup";
    }

    @PostMapping
    public String processSignUp(@Valid @ModelAttribute("signUp") SignUp signUp,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "public/signup";
        }
        try {
            userService.registerUser(signUp);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "public/signup";
        }
        return "redirect:/onlinemarket/public/login?registered";
    }
}
