package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {

        this.userService = userService;
    }

    @GetMapping("/simpel-login")
    public String showLoginForm(){

        return "login"; // viser Login-html via. Thymeleaf
    }

    @GetMapping("/logout")
    public String logoutMessage(){

        return "redirect:/login?logout=true";
    }
}
