package com.example.alphasolutionsv2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Bruger application-test.properties
class ProjectAccessTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void testRedirectToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/my-projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void testAccessMyProjectAsAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/my-projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"Admin"})
    void testAccessMyProjectAsAdmin() throws Exception {
        mockMvc.perform(get("/my-projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("loggedInUser"));
    }
}