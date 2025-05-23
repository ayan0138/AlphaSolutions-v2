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

class ApplicationUserDetailsServiceTest {
    private UserRepository userRepository;
    private ApplicationUserDetailsService applicationUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        applicationUserDetailsService = new ApplicationUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        Role role = new Role(1L, "admin");
        User user = new User(1L, "najib", "najib@firma.dk", "hashedpw", role);

        when(userRepository.findByUsername("najib")).thenReturn(Optional.of(user));

        UserDetails userDetails = applicationUserDetailsService.loadUserByUsername("najib");

        assertEquals("najib", userDetails.getUsername());
        assertEquals("hashedpw", userDetails.getPassword());

        // Test begge authorities (ROLE_ prefixed og original)
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
        assertEquals(2, userDetails.getAuthorities().size()); // Skal have begge
    }

    @Test
    void testLoadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("ukendt")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                applicationUserDetailsService.loadUserByUsername("ukendt"));
    }

    @Test
    void testLoadUserByUsername_shouldReturnUserWithEmptyAuthorities_whenRoleIsNull() {
        User user = new User(1L, "najib", "najib@firma.dk", "hashedpw", null); // Ingen rolle

        when(userRepository.findByUsername("najib")).thenReturn(Optional.of(user));

        UserDetails userDetails = applicationUserDetailsService.loadUserByUsername("najib");

        assertEquals("najib", userDetails.getUsername());
        assertEquals("hashedpw", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty()); // Tom authorities liste
    }

    @Test
    void testLoadUserByUsername_shouldReturnUserWithEmptyAuthorities_whenRoleNameIsNull() {
        Role role = new Role(1L, null); // Rolle uden navn
        User user = new User(1L, "najib", "najib@firma.dk", "hashedpw", role);

        when(userRepository.findByUsername("najib")).thenReturn(Optional.of(user));

        UserDetails userDetails = applicationUserDetailsService.loadUserByUsername("najib");

        assertEquals("najib", userDetails.getUsername());
        assertEquals("hashedpw", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty()); // Tom authorities liste
    }
}