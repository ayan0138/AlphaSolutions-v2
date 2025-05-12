package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@Controller
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "login"; // viser Login-html via. Thymeleaf
    }

    @GetMapping("/logout")
    public String logoutMessage(){
        return "redirect:/login?logout=true";
    }
}