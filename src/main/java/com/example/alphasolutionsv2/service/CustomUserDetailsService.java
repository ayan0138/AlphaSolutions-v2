package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user  = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Bruger ikke fundet: " + username));

        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null ||
                user.getRole() == null) {
            throw new UsernameNotFoundException("Brugerdata mangler: " + username);
        }

        String role = "ROLE_" + user.getRole().getRoleName().toUpperCase();

        return new org.springframework.security.core.userdetails.User(
          user.getUsername(),
          user.getPassword(),
          List.of(new SimpleGrantedAuthority(role))
        );
    }
}
