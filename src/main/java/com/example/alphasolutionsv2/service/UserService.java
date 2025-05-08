package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepo;

    public UserService(UserRepository userRepo){
        this.userRepo=userRepo;
    }

    public User authenticate(String username, String password) {
        return userRepo.findByUsernameAndPassword(username, password);
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