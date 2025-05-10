package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo,  BCryptPasswordEncoder passwordEncoder) {
        this.userRepo=userRepo;
        this.passwordEncoder=passwordEncoder;
    }

    public User authenticate(String username, String rawPassword) {
        User user = userRepo.findByUsername(username);
        if(user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    // Added method to get user by ID
    public Optional<User> getUserById(Long userId) {
        User user = userRepo.findById(userId);
        return Optional.ofNullable(user);
    }

    // Method to check if a user is an admin
    public boolean isAdmin(Long userId) {
        User user = userRepo.findById(userId);
        // Check if user has role and role name is ADMIN
        return user != null && user.getRole() != null && "ADMIN".equals(user.getRole().getRoleName());
    }

    // Method to get all users
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}