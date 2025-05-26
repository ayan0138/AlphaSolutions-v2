package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.TaskService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ProjectController.class)
@Import({ProjectControllerTest.TestConfig.class, com.example.alphasolutionsv2.config.SecurityConfig.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @TestConfiguration
    static class TestConfig {
        @Bean public UserService userService() { return Mockito.mock(UserService.class); }
        @Bean public ProjectService projectService() { return Mockito.mock(ProjectService.class); }
        @Bean public TaskService taskService() { return Mockito.mock(TaskService.class); }
        @Bean public SubProjectService subProjectService() { return Mockito.mock(SubProjectService.class); }
    }

    // ========================================
    // === VISNING AF PROJEKTER ===
    // ========================================

    // GET: /my-projects – visning af brugerens projekter
    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldReturnProjectList_whenUserIsAuthenticated() throws Exception {
        Role medarbejder = new Role("MEDARBEJDER");
        User fakeUser = new User(3L,"najib", "najib@firma.dk", "hashed", medarbejder);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(fakeUser));
        when(projectService.getProjectsByUserId(3L)).thenReturn(List.of());

        mockMvc.perform(get("/my-projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/my-projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    // GET: /projects/{id} – brugeren har adgang
    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldShowProjectDetails_whenUserHasAccess() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);
        Project project = new Project();
        project.setProjectId(1L);
        project.setCreatedBy(user);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));
        when(projectService.userCanViewProject(3L, 1L)).thenReturn(true);

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/project-details"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    // GET: /projects/{id} – projekt findes ikke
    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenProjectNotFound() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/projects/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects?error=Projekt+ikke+fundet"));
    }

    // GET: /projects/{id} – bruger har ikke adgang
    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenUserHasNoAccessToProject() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);
        Project project = new Project();
        project.setProjectId(2L);
        project.setCreatedBy(new User(2L, "marcus", "marcus@firma.dk", "hashed", new Role("Projektleder")));
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(2L)).thenReturn(Optional.of(project));
        when(projectService.userCanViewProject(3L, 2L)).thenReturn(false);

        mockMvc.perform(get("/projects/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects?error=Ingen+rettigheder"));
    }

    // ========================================
    // === REDIGERING AF PROJEKTER ===
    // ========================================

    // GET: /projects/{id}/edit – admin har adgang
    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldShowEditForm_whenUserHasPermission() throws Exception {
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", new Role("ADMIN"));
        Project project = new Project();
        project.setProjectId(5L);
        project.setCreatedBy(user);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(5L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        mockMvc.perform(get("/projects/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/edit-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    // GET: /projects/{id}/edit – bruger har ikke adgang
    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenUserCannotEditProject() throws Exception {
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", new Role("MEDARBEJDER"));
        Project project = new Project();
        project.setProjectId(7L);
        project.setCreatedBy(new User(1L, "admin", "admin@example.com", "hashed", new Role("Admin")));
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(7L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(false);

        mockMvc.perform(get("/projects/7/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/my-projects?error=*"));
    }

    // POST: /projects/{id}/edit – korrekt data, rediger succes
    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldUpdateProjectAndRedirect_whenValidInputAndPermission() throws Exception {
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", new Role("ADMIN"));
        Project project = new Project();
        project.setProjectId(10L);
        project.setCreatedBy(user);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(10L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);
        when(projectService.updateProjectDetails(any(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/projects/10/edit")
                        .param("name", "Nyt navn")
                        .param("description", "Ny beskrivelse")
                        .param("startDateStr", "2025-05-01")
                        .param("endDateStr", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/10"));
    }

    // POST: /projects/{id}/edit – fejl i dato, vis formular igen
    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldReturnToForm_whenUpdateFails() throws Exception {
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", new Role("ADMIN"));
        Project project = new Project();
        project.setProjectId(11L);
        project.setCreatedBy(user);
        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(11L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);
        when(projectService.updateProjectDetails(any(), any(), any(), any(), any())).thenReturn("Ugyldige datoer");

        mockMvc.perform(post("/projects/11/edit")
                        .param("name", "Navn")
                        .param("description", "Fejl")
                        .param("startDateStr", "2025-06-01")
                        .param("endDateStr", "2025-05-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/edit-project"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("project"));
    }

    // POST: /projects/{id}/edit – fejl i datoformat
    @Test
    @WithMockUser(username = "marcus", roles = {"PROJEKTLEDER"})
    void testShouldReturnToEditForm_whenDateParsingFails() throws Exception {
        User user = new User(2L, "marcus", "marcus@firma.dk", "hashed", new Role("PROJEKTLEDER"));
        Project project = new Project();
        project.setProjectId(5L);
        project.setCreatedBy(user);
        when(userService.getUserByUsername("marcus")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(5L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);
        when(projectService.updateProjectDetails(any(), any(), any(), any(), any())).thenReturn("Ugyldig datoformat");

        mockMvc.perform(post("/projects/5/edit")
                        .param("name", "Redesign")
                        .param("description", "Fejltest")
                        .param("startDateStr", "ugyldig")
                        .param("endDateStr", "også-ugyldig"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/edit-project"))
                .andExpect(model().attributeExists("error"));
    }

    // ========================================
    // === OPRETTELSE AF PROJEKT ===
    // ========================================

    // GET: /projects/create – vis formular
    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testShowCreateProjectForm_shouldReturnFormWithMode() throws Exception {
        Role role = new Role("PROJEKTLEDER");
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", role);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/projects/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/create-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attribute("loggedInUser", user));
    }

    // POST: /projects/create – succesfuld oprettelse
    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testCreateProject_shouldRedirectToMyProjectsOnSuccess() throws Exception {
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", new Role("PROJEKTLEDER"));
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
        doNothing().when(projectService).createProject(any(Project.class));

        mockMvc.perform(post("/projects/create")
                        .param("name", "Projekt x")
                        .param("description", "Beskrivelse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects"))
                .andExpect(flash().attribute("success", "Projekt oprettet!"));
    }

    // POST: /projects/create – fejl ved oprettelse
    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testCreateProject_shouldRedirectBackOnError() throws Exception {
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", new Role("PROJEKTLEDER"));
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Noget gik galt")).when(projectService).createProject(any(Project.class));

        mockMvc.perform(post("/projects/create")
                        .param("name", "Projekt X")
                        .param("description", "Fejl test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/create"))
                .andExpect(flash().attribute("error", "Noget gik galt"));
    }
}
