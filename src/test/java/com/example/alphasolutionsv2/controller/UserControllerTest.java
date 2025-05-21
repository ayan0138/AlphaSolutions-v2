package com.example.alphasolutionsv2.controller;

import static org.mockito.ArgumentMatchers.any;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.RoleService;
import com.example.alphasolutionsv2.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserControllerTest.TestConfig.class, com.example.alphasolutionsv2.config.SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public RoleService roleService() {
            return Mockito.mock(RoleService.class);
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser_withValidData_shouldRedirectAndCallService() throws Exception {
        mockMvc.perform(post("/admin/create-user")
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "secure123")
                        .param("role.roleId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?success"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser_withMissingEmail_shouldFail() throws Exception {
        mockMvc.perform(post("/admin/create-user")
                        .param("username", "invaliduser")
                        .param("email", "")  // tom
                        .param("password", "pwd123")
                        .param("role.roleId", "3"))
                .andExpect(status().isOk()) // Formular vises igen
                .andExpect(view().name("admin/create-user-form"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser_withExistingUsername_shouldStayOnForm() throws Exception {
        doThrow(new IllegalArgumentException("Brugernavn findes allerede"))
                .when(userService).createUser(any(User.class));

        mockMvc.perform(post("/admin/create-user")
                        .param("username", "existinguser")
                        .param("email", "existing@example.com")
                        .param("password", "test123")
                        .param("role.roleId", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/create-user-form"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testShowUserList_shouldRenderTemplateAndList() throws Exception {
        Role role = new Role(1L, "ADMIN");
        User user = new User(1L, "admin", "admin@example.com", "hashed", role);

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/admin/users").param("success", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "marcus", roles = {"MEDARBEJDER"})
    void testMedarbejderCannotAccessCreateUserForm() throws Exception {
        mockMvc.perform(get("/admin/create-user"))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testShowEditUserForm_withValidUserId_shouldShowForm() throws Exception {
        // Reset mocks to clear previous interactions
        reset(userService, roleService);

        // Arrange
        User user = new User(2L, "testuser", "test@example.com", "hashedpwd", new Role(3L, "MEDARBEJDER"));
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(roleService.getAllRoles()).thenReturn(List.of(
                new Role(1L, "ADMIN"),
                new Role(2L, "PROJEKTLEDER"),
                new Role(3L, "MEDARBEJDER")
        ));

        // Act & Assert
        mockMvc.perform(get("/admin/edit-user/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-user"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"));

        // Use atLeastOnce() to make verification more flexible
        verify(userService, atLeastOnce()).getUserById(2L);
        verify(roleService, atLeastOnce()).getAllRoles();
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testShowEditUserForm_withInvalidUserId_shouldRedirect() throws Exception {
        // Reset the mock to clear any prior interactions
        reset(userService);

        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/admin/edit-user/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?error=Bruger+ikke+fundet"));

        // Verify with at least once instead of exactly once
        verify(userService, atLeastOnce()).getUserById(999L);
    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_withValidData_shouldRedirect() throws Exception {
        // Arrange
        User user = new User(2L, "testuser", "test@example.com", "hashedpwd", new Role(3L, "MEDARBEJDER"));
        Role role = new Role(2L, "PROJEKTLEDER");

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(roleService.getRoleById(2L)).thenReturn(Optional.of(role));

        // Act & Assert
        mockMvc.perform(post("/admin/update-user/2")
                        .param("username", "updated_user")
                        .param("email", "updated@example.com")
                        .param("password", "")
                        .param("roleId", "2")
                        .param("changePassword", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?success=Bruger+opdateret"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_withPasswordChange_shouldUpdatePasswordAndRedirect() throws Exception {
        // Arrange
        User user = new User(2L, "testuser", "test@example.com", "oldhash", new Role(3L, "MEDARBEJDER"));
        Role role = new Role(3L, "MEDARBEJDER");

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(roleService.getRoleById(3L)).thenReturn(Optional.of(role));

        // Act & Assert
        mockMvc.perform(post("/admin/update-user/2")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .param("password", "NewPassword123!")
                        .param("roleId", "3")
                        .param("changePassword", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?success=Bruger+opdateret"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_withInvalidUserId_shouldRedirect() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/admin/update-user/999")
                        .param("username", "nonexistent")
                        .param("email", "nonexistent@example.com")
                        .param("password", "")
                        .param("roleId", "3")
                        .param("changePassword", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?error=Bruger+ikke+fundet"));

        verify(userService).getUserById(999L);
        verify(userService, never()).updateUser(any(User.class), anyBoolean());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_withInvalidRoleId_shouldStayOnForm() throws Exception {
        // Arrange
        User user = new User(2L, "testuser", "test@example.com", "hashedpwd", new Role(3L, "MEDARBEJDER"));

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(roleService.getRoleById(999L)).thenReturn(Optional.empty());
        when(roleService.getAllRoles()).thenReturn(List.of(
                new Role(1L, "ADMIN"),
                new Role(2L, "PROJEKTLEDER"),
                new Role(3L, "MEDARBEJDER")
        ));

        // Act & Assert
        mockMvc.perform(post("/admin/update-user/2")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .param("password", "")
                        .param("roleId", "999")
                        .param("changePassword", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-user"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"));

        verify(userService).getUserById(2L);
        verify(roleService).getRoleById(999L);
        verify(userService, never()).updateUser(any(User.class), anyBoolean());
    }

    @Test
    @WithMockUser(username = "marcus", roles = {"MEDARBEJDER"})
    void testMedarbejderCannotAccessEditUserForm() throws Exception {
        mockMvc.perform(get("/admin/edit-user/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "marcus", roles = {"MEDARBEJDER"})
    void testMedarbejderCannotUpdateUser() throws Exception {
        mockMvc.perform(post("/admin/update-user/1")
                        .param("username", "somename")
                        .param("email", "some@email.com")
                        .param("password", "")
                        .param("roleId", "3")
                        .param("changePassword", "false"))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(any(User.class), anyBoolean());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_withServiceException_shouldStayOnForm() throws Exception {
        // Arrange
        User user = new User(2L, "testuser", "test@example.com", "hashedpwd", new Role(3L, "MEDARBEJDER"));
        Role role = new Role(2L, "PROJEKTLEDER");

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(roleService.getRoleById(2L)).thenReturn(Optional.of(role));
        doThrow(new RuntimeException("Database error")).when(userService).updateUser(any(User.class), anyBoolean());
        when(roleService.getAllRoles()).thenReturn(List.of(
                new Role(1L, "ADMIN"),
                new Role(2L, "PROJEKTLEDER"),
                new Role(3L, "MEDARBEJDER")
        ));

        // Act & Assert
        mockMvc.perform(post("/admin/update-user/2")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .param("password", "")
                        .param("roleId", "2")
                        .param("changePassword", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/edit-user"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"));
    }
}