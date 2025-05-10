package com.example.alphasolutionsv2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Bruger application-test.properties
class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private JdbcTemplate jdbcTemplate;

    @Test
    void testShouldLoginSuccessfullyAndRedirectToMyProjects() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "najib")
                        .param("password", "1234")) // Raw password (skal matche hash i testdata)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects"))
                .andExpect(request().sessionAttribute("userID", notNullValue()));
    }

    @Test
    void testShouldFailLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "najib")
                        .param("password", "forkert"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    void testShouldRedirectToLoginIfAccessingProjectsWithoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/my-projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}