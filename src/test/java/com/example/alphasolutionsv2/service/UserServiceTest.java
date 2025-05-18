package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserServiceTest {
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_shouldHashPasswordAndSaveUser() {
        // Arrange
        Role role = new Role(3L, "MEDARBEJDER");
        User user = new User(null, "testuser", "test@example.com", "plaintext123", role);

        // 👉 MOCK før createUser kaldes
        when(passwordEncoder.encode("plaintext123")).thenReturn("hashed123");
        when(passwordEncoder.matches("plaintext123", "hashed123")).thenReturn(true);

        // Act
        userService.createUser(user);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).createUser(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("MEDARBEJDER", savedUser.getRole().getRoleName());

        // Nu giver det mening
        assertEquals("hashed123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("plaintext123", savedUser.getPassword()));
    }


    @Test
    void authenticate_returnsUser_whenCredentialsAreValid() {
        String username = "testuser";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = new Role(1L, "ADMIN");
        User mockUser = new User(1L, username, "test@example.com", encodedPassword, role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        Optional<User> result = userService.authenticate(username, rawPassword);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void authenticate_returnsEmptyOptional_whenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("nonexistent", "anyPassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_returnsEmptyOptional_whenPasswordIsWrong() {
        String username = "testuser";
        String encodedPassword = passwordEncoder.encode("correctPassword");
        Role role = new Role(1L, "Admin");
        User mockUser = new User(1L, username, "test@example.com", encodedPassword, role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.authenticate(username, "wrongPassword");

        assertTrue(result.isEmpty());
    }
}