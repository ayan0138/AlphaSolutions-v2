package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.ProjectRepository;
import com.example.alphasolutionsv2.repository.RoleRepository;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // 1. Gem rolle
        Role role = new Role( 7L,"Medarbejder");
        roleRepository.saveRole(role);

        // 2. Gem bruger
        testUser = new User(null, "najib", "najib@firma.dk", "test123", role);
        userRepository.saveUser(testUser);
        User persistedUser = userRepository.findByUsername("najib").orElseThrow();

        // 3. Gem projekt
        testProject = new Project();
        testProject.setName("Eksamensprojekt");
        testProject.setDescription("Integrationstest af controller");
        testProject.setStartDate(LocalDate.of(2025,5,31));
        testProject.setEndDate(LocalDate.of(2025,6,30));
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setCreatedBy(persistedUser);
        projectRepository.save(testProject);
    }

    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void testShouldReturnProjectDetails_whenUserHasAcces() throws Exception {
        mockMvc.perform(get("/projects/" + testProject.getProjectId()))
                .andExpect(status().isOk())
                .andExpect(view().name("project-details"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Eksamensprojekt")));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"Admin"})
    void testShouldRedirectToDetails_whenUpdateIsValid() throws Exception {
        mockMvc.perform(post("/projects/" + testProject.getProjectId() + "/edit")
                        .param("name", "Opdateret navn")
                        .param("description", "Opdateret beskrivelse")
                        .param("startDate", "2025-05-01")
                        .param("endDate", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/" + testProject.getProjectId()));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"Admin"})
    void testShouldReturnToForm_whenUpdateIsInvalid() throws Exception {
        mockMvc.perform(post("/projects/" + testProject.getProjectId() + "/edit")
                        .param("name", "Fejl-projekt")
                        .param("description", "Fejlbeskrivelse")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2025-05-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("error"));
    }
}