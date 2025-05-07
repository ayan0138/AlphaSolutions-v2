package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepo;

    public UserService(UserRepository userRepo){
        this.userRepo=userRepo;
    }

    public User authenticate(String username, String password) {
        return userRepo.findByUsernameAndPassword(username, password);
    }
}
