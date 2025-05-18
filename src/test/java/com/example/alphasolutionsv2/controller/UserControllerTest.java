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
}