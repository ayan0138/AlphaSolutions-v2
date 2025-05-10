package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.AlphaSolutionsV2Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AlphaSolutionsV2Application.class)
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToLogin_whenUserNotInSession_onMyProjects() throws Exception {
        mockMvc.perform(get("/my-projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldRedirectToLogin_whenUserNotInSession_onProjectDetails() throws Exception {
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldRedirectToLogin_whenUserNotInSession_onEditProjectForm() throws Exception {
        mockMvc.perform(get("/projects/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldReturnProjectList_whenUserInSession() throws Exception {
        mockMvc.perform(get("/my-projects")
                        .sessionAttr("userID", 1L)) // Husk: bruger med ID 1L skal eksistere i DB
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("projects"));
    }
}