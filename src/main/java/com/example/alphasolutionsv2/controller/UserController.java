package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.RoleService;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final RoleService roleService; // Rollerne hentes dynamisk til dropdown

    public UserController(UserService userService, RoleService roleService) {
        this.userService =  userService;
        this.roleService = roleService;

    }

    // Vis formularen
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/create-user")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user",  new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/create-user-form"; // HTML side.
    }

    // Modtag POST fra formular
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/create-user")
    public String createUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.createUser(user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/create-user-form";
        }
        return "redirect:/admin/users?success";
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/users")
    public String showAllUsers(Model model, @RequestParam(required = false) String success) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("success", success != null);
        return "admin/user-list";
    }



}
