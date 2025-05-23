package com.example.alphasolutionsv2.config;

import com.example.alphasolutionsv2.service.ApplicationUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class UserSecurityConfig {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserSecurityConfig(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Only create this bean if it doesn't already exist
    @Bean
    public UserDetailsService userDetailsService(com.example.alphasolutionsv2.repository.UserRepository userRepository) {
        return new ApplicationUserDetailsService(userRepository);
    }
}