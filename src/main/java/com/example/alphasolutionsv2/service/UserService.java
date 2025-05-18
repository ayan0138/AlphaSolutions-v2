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

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo=userRepo;
        this.passwordEncoder=passwordEncoder;
    }

    // Task 8.1: En metode til at oprette en ny bruger
    public  void createUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userRepo.createUser(user); // videresender objektet
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepo.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // Metode til at finde bruger via. User ID
    public Optional<User> getUserById(Long userId) {
        return userRepo.findById(userId);
    }

    // Metode til at tjekke om bruger er administrator
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

    public void updateUser(User user, boolean passwordChanged) {
        // Hash password kun hvis det er ændret
       if(passwordChanged) {
           String hashedPassword = passwordEncoder.encode(user.getPassword());
           user.setPassword(hashedPassword);
       }
        // Gem brugeren (Kald repository-metoden der håndterer UPDATE via user_id)
        userRepo.saveUser(user);
    }
}