package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Remove @Service annotation - we'll create it as a bean in UserSecurityConfig
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ApplicationUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            // Add the ROLE_ prefix to role names
            String roleName = "ROLE_" + user.getRole().getRoleName().toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleName));

            // Also add the original role name to ensure backward compatibility
            authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName().toUpperCase()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}