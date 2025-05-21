package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.RoleService;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/edit-user/{userId}")
    public String showEditUserForm(@PathVariable Long userId, Model model) {
        // Log the user ID to verify it's correct
        System.out.println("Edit user ID: " + userId);

        Optional<User> userOpt = userService.getUserById(userId);

        if (userOpt.isEmpty()) {
            return "redirect:/admin/users?error=Bruger+ikke+fundet";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("roles", roleService.getAllRoles());

        return "admin/edit-user";  // Match your template name exactly
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/admin/update-user/{userId}")
    public String updateUser(
            @PathVariable Long userId,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Long roleId,
            @RequestParam(required = false) boolean changePassword,
            Model model) {

        try {
            // Get the current user
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return "redirect:/admin/users?error=Bruger+ikke+fundet";
            }

            // Get the role
            Optional<Role> roleOpt = roleService.getRoleById(roleId);
            if (roleOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Ugyldig rolle valgt");
                model.addAttribute("user", userOpt.get());
                model.addAttribute("roles", roleService.getAllRoles());
                return "admin/edit-user";
            }

            // Update user object
            User user = userOpt.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(roleOpt.get()); // Extract the Role from Optional

            // Set password if changing
            if (changePassword && !password.isEmpty()) {
                user.setPassword(password);
            }

            // Update the user
            userService.updateUser(user, changePassword && !password.isEmpty());

            return "redirect:/admin/users?success=Bruger+opdateret";
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            model.addAttribute("errorMessage", "Fejl ved opdatering: " + e.getMessage());

            // Reload the user and roles
            User user = userService.getUserById(userId).orElse(new User());
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.getAllRoles());

            return "admin/edit-user";
        }
    }
}