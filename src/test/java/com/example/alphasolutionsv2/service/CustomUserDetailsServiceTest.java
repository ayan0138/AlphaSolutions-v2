package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {
    private UserRepository userRepository;
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        Role role = new Role(1L, "admin");
        User user = new User(1L, "najib", "najib@firma.dk", "hashedpw", role);

        when(userRepository.findByUsername("najib")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("najib");

        assertEquals("najib", userDetails.getUsername());
        assertEquals("hashedpw", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("ukendt")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("ukendt"));
    }

    @Test
    void testLoadUserByUsername_shouldThrow_whenUserDataIsIncomplete() {
        User user = new User();
        user.setUsername("najib");
        // mangler password, email og rolle

        when(userRepository.findByUsername("najib")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("najib"));
    }
}