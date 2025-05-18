package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.config.SecurityConfig;
import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(SecurityConfig.class)
@Transactional  // Sikrer at testdata rulles tilbage efter testen
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserServiceIntegrationTest {


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testCreateUser_savesUserWithHashedPasswordAndCorrectRole() {
        // Arrange
        Role role = new Role(3L, "MEDARBEJDER"); // Brug ID og navn der findes i din roles-tabel
        User newUser = new User(null, "testbruger", "test@eksempel.dk", "hemmelig123", role);

        // Act
        userService.createUser(newUser);

        // Assert
        Optional<User> savedUser = userRepository.findByUsername("testbruger");
        assertTrue(savedUser.isPresent());

        User actual = savedUser.get();
        assertEquals("testbruger", actual.getUsername());
        assertEquals("test@eksempel.dk", actual.getEmail());
        assertEquals("MEDARBEJDER", actual.getRole().getRoleName());

        // Tjek at password er hashed (ikke samme som input)
        assertNotEquals("hemmelig123", actual.getPassword());
        assertTrue(passwordEncoder.matches("hemmelig123", actual.getPassword()));
    }

    @Test
    void testUpdateUser_passwordChanged_shouldHashNewPassword() {
        // Arrange
        Role role = new Role(2L, "PROJEKTLEDER");
        User original = new User(null, "editme", "edit@firma.dk", "oldpass", role);
        userService.createUser(original);

        Optional<User> fromDb = userRepository.findByUsername("editme");
        assertTrue(fromDb.isPresent());

        User toUpdate = fromDb.get();

        // Simulér ny adgangskode
        toUpdate.setPassword("nyKode123");

        // Act
        userService.updateUser(toUpdate, true); // vigtigt: passwordChanged = true

        // Assert
        Optional<User> updated = userRepository.findByUsername("editme");
        assertTrue(updated.isPresent());

        User actual = updated.get();
        assertNotEquals("nyKode123", actual.getPassword()); // skal være hashet
        assertTrue(passwordEncoder.matches("nyKode123", actual.getPassword())); // ← denne fejlede
    }
}