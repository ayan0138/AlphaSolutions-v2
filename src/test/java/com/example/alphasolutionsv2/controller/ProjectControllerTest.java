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
    static class TestConfig{
        @Bean
        public UserService userService(){
            return Mockito.mock(UserService.class);
        }

        @Bean
        public ProjectService projectService(){
            return Mockito.mock(ProjectService.class);
        }

        @Bean
        public TaskService taskService() {
            return Mockito.mock(TaskService.class);
        }

        @Bean
        public SubProjectService subProjectService() {
            return Mockito.mock(SubProjectService.class);
        }
    }

    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldReturnProjectList_whenUserIsAuthenticated() throws Exception {
        // Arrange - mock bruger
        Role medarbejder = new Role("MEDARBEJDER");
        User fakeUser = new User(3L,"najib", "najib@firma.dk", "hashed",  medarbejder);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(fakeUser));
        when(projectService.getProjectsByUserId(3L)).thenReturn(List.of());


        // Act + Assert
        mockMvc.perform(get("/my-projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("loggedInUser"));

    }

    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldShowProjectDetails_whenUserHasAccess() throws Exception {
        // Arrange
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);

        Project project = new Project();
        project.setProjectId(1L);
        project.setName("Testprojekt");
        project.setDescription("Beskrivelse");
        project.setCreatedBy(user); // Brugeren har selv oprettet projektet

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));
        when(projectService.userCanViewProject(3L, 1L)).thenReturn(true);

        // Act + Assert
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-details"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenProjectNotFound() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(99L)).thenReturn(Optional.empty()); // Projekt findes ikke

        mockMvc.perform(get("/projects/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects?error=Projekt+ikke+fundet"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenUserHasNoAccessToProject() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);

        User creator = new User(2L, "marcus", "marcus@firma.dk", "hashed", new Role("Projektleder"));
        Project project = new Project();
        project.setProjectId(2L);
        project.setName("Skjult Projekt");
        project.setCreatedBy(creator);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(2L)).thenReturn(Optional.of(project));
        when(projectService.userCanViewProject(3L, 2L)).thenReturn(false); // Ingen adgang

        mockMvc.perform(get("/projects/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects?error=Ingen+rettigheder"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldShowEditForm_whenUserHasPermission() throws Exception {
        Role role = new Role("ADMIN");
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", role);

        Project project = new Project();
        project.setProjectId(5L);
        project.setName("Test projekt");
        project.setCreatedBy(user); // Bruger ejer projekt

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(5L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        mockMvc.perform(get("/projects/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"MEDARBEJDER"})
    void testShouldRedirect_whenUserCannotEditProject() throws Exception {
        Role role = new Role("MEDARBEJDER");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);

        User creator = new User(1L, "admin", "admin@example.com", "hashed", new Role("Admin"));
        Project project = new Project();
        project.setProjectId(7L);
        project.setName("Privat projekt");
        project.setCreatedBy(creator);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(7L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(false);

        mockMvc.perform(get("/projects/7/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/my-projects?error=*"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldUpdateProjectAndRedirect_whenValidInputAndPermission() throws Exception {
        Role role = new Role("ADMIN");
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", role);

        Project project = new Project();
        project.setProjectId(10L);
        project.setName("Gammelt navn");
        project.setCreatedBy(user);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(10L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        when(projectService.updateProjectDetails(any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null); // ingen fejl

        mockMvc.perform(post("/projects/10/edit")
                        .param("name", "Nyt navn")
                        .param("description", "Ny beskrivelse")
                        .param("startDateStr", "2025-05-01")   // ✅ korrekt
                        .param("endDateStr", "2025-06-01"))    // ✅ korrekt
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/10"));
    }


    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShouldReturnToForm_whenUpdateFails() throws Exception {
        Role role = new Role("ADMIN");
        User user = new User(1L, "najib", "najib@firma.dk", "hashed", role);

        Project project = new Project();
        project.setProjectId(11L);
        project.setName("Fejl projekt");
        project.setCreatedBy(user);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(11L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        doReturn("Ugyldige datoer")
                .when(projectService)
                .updateProjectDetails(any(), anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/projects/11/edit")
                        .param("name", "Navn")
                        .param("description", "Fejlbeskrivelse")
                        .param("startDateStr", "2025-06-01")   // ✅ Rettet
                        .param("endDateStr", "2025-05-01"))    // ✅ Rettet
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("loggedInUser"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testShouldShowProjectDetails_whenUserIsAdmin() throws Exception {
        Role adminRole = new Role("ADMIN");
        User admin = new User(1L, "admin", "admin@example.com", "hashed", adminRole);

        Project fakeProject = new Project();
        fakeProject.setProjectId(100L);
        fakeProject.setCreatedBy(new User(2L, "creator", "creator@mail.com",
                "pw", new Role("Medarbejder")));

        when(userService.getUserByUsername("admin")).thenReturn(Optional.of(admin));
        when(projectService.getProjectById(100L)).thenReturn(Optional.of(fakeProject));
        when(projectService.userCanViewProject(1L, 100L)).thenReturn(false); // Admin behøver ikke dette

        mockMvc.perform(get("/projects/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("project-details"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "marcus", roles = {"PROJEKTLEDER"})
    void testShouldReturnToEditForm_whenDateParsingFails() throws Exception {
        Role role = new Role("PROJEKTLEDER");
        User user = new User(2L, "marcus", "marcus@firma.dk", "hashed", role);
        Project project = new Project();
        project.setProjectId(5L);
        project.setCreatedBy(user);

        when(userService.getUserByUsername("marcus")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(5L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        when(projectService.updateProjectDetails(any(),
                any(), any(), any(), any()))
                .thenReturn("Ugyldig datoformat");

        mockMvc.perform(post("/projects/5/edit")
                        .param("name", "Redesign")
                        .param("description", "Fejltest")
                        .param("startDateStr", "ugyldig")
                        .param("endDateStr", "også-ugyldig"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testShowCreateProjectForm_shouldReturnFormWithMode() throws Exception{
        Role role = new Role("PROJEKTLEDER");
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", role);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/projects/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attribute("loggedInUser", user));
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testCreateProject_shouldRedirectToMyProjectsOnSuccess() throws Exception{
        Role role = new Role("PROJEKTLEDER");
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", role);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
        doNothing().when(projectService).createProject(any(Project.class));

        mockMvc.perform(post("/projects/create")
                .param("name", "Projekt x")
                .param("description", "Beskrivelse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-projects"))
                .andExpect(flash().attribute("success", "Projekt oprettet!"));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"PROJEKTLEDER"})
    void testCreateProject_shouldRedirectBackOnError() throws Exception {
        Role role = new Role("PROJEKTLEDER");
        User user = new User(1L, "testuser", "mail@test.dk", "hashed", role);

        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Noget gik galt"))
                .when(projectService).createProject(any(Project.class));

        mockMvc.perform(post("/projects/create")
                        .param("name", "Projekt X")
                        .param("description", "Fejl test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/create"))
                .andExpect(flash().attribute("error", "Noget gik galt"));
    }

    @Test
    @WithMockUser(username = "najib", roles = {"ADMIN"})
    void testShowEditForm_shouldReturnEditViewWithProject() throws Exception {
        Role role = new Role("ADMIN");
        User user = new User(3L, "najib", "najib@firma.dk", "hashed", role);

        Project project = new Project();
        project.setProjectId(3L);
        project.setName("Testprojekt");
        project.setCreatedBy(user);

        when(userService.getUserByUsername("najib")).thenReturn(Optional.of(user));
        when(projectService.getProjectById(3L)).thenReturn(Optional.of(project));
        when(projectService.userCanEditProject(user, project)).thenReturn(true);

        mockMvc.perform(get("/projects/3/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("loggedInUser"));
    }
}
