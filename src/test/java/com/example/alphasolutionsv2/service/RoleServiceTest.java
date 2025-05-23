package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {
    @Test
    void testGetAllRoles_shouldReturnAllRoles() {
        // Arrange
        RoleRepository mockRepo = mock(RoleRepository.class);
        RoleService roleService = new RoleService(mockRepo);

        List<Role> expectedRoles = Arrays.asList(
                new Role(1L, "Admin"),
                new Role(2L, "Medarbejder")
        );

        when(mockRepo.findAll()).thenReturn(expectedRoles);

        // Act
        List<Role> actualRoles = roleService.getAllRoles();

        // Assert
        assertEquals(2, actualRoles.size());
        assertEquals("Admin", actualRoles.get(0).getRoleName());
        verify(mockRepo, times(1)).findAll();
    }
}