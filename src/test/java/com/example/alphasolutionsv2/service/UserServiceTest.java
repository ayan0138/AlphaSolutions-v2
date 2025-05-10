package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testShouldAuthenticateUserWithCorrectCredentials() {
        // Arrange
        String username = "najib";
        String rawPassword = "secret";
        String hashedPassword = "$2a$10$abc..."; // fiktiv hash

        User mockUser = new User(1L, username, "najib@test.com", hashedPassword, new Role("Medarbejder"));
        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        // Act
        User result = userService.authenticate(username, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals("najib", result.getUsername());
    }

    @Test
    void testShouldFailAuthenticationWithWrongPassword() {
        // Arrange
        String username = "najib";
        String rawPassword = "wrong";
        String hashedPassword = "$2a$10$abc...";

        User mockUser = new User(1L, username, "najib@test.com", hashedPassword, new Role("Medarbejder"));
        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // Act
        User result = userService.authenticate(username, rawPassword);

        // Assert
        assertNull(result);
    }

    @Test
    void testShouldReturnNullIfUserNotFound() {
        when(userRepository.findByUsername("ukendt")).thenReturn(null);
        assertNull(userService.authenticate("ukendt", "any"));
    }



}