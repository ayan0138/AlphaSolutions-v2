package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo,  PasswordEncoder passwordEncoder) {
        this.userRepo=userRepo;
        this.passwordEncoder=passwordEncoder;
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepo.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // Added method to get user by ID
    public Optional<User> getUserById(Long userId) {
        return userRepo.findById(userId);
    }

    // Method to check if a user is an admin
    public boolean isAdmin(Long userId) {
        return userRepo.findById(userId)
                .map( user -> user.getRole() != null &&
                        "ADMIN".equalsIgnoreCase(user.getRole().getRoleName()))
                .orElse(false);
    }



    // Method to get all users
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}