package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.*;
import com.example.alphasolutionsv2.service.ReportService;
import com.example.alphasolutionsv2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Project testProject;
    private ProjectReport testReport;

    @BeforeEach
    void setUp() {
        // Setup test role
        Role testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName("Projektleder");

        // Setup test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(testRole);

        // Setup test project with createdBy user
        testProject = new Project();
        testProject.setProjectId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStartDate(LocalDate.now());
        testProject.setEndDate(LocalDate.now().plusDays(30));
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setCreatedBy(testUser); // Add this line!

        // Setup test report summary
        ReportSummary summary = new ReportSummary(
                20.0,
                8000.0,
                2,
                Map.of("PENDING", 1, "IN_PROGRESS", 1)
        );

        // Setup test report
        testReport = new ProjectReport(
                testProject,
                List.of(),
                List.of(),
                Map.of("Test SubProject", List.of()),
                summary,
                LocalDateTime.now(),
                testUser
        );
    }

    @Test
    void testGenerateProjectReport_Success() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportService.generateProjectReport(eq(1L), any(User.class))).thenReturn(testReport);

        // Act & Assert
        mockMvc.perform(get("/reports/project/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("project-report"))
                .andExpect(model().attribute("report", testReport))
                .andExpect(model().attribute("loggedInUser", testUser));
    }

    @Test
    void testGenerateProjectReport_UserNotFound() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/reports/project/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testGenerateProjectReport_ProjectNotFound() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportService.generateProjectReport(eq(1L), any(User.class)))
                .thenThrow(new IllegalArgumentException("Projekt ikke fundet med ID: 1"));

        // Act & Assert
        mockMvc.perform(get("/reports/project/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("error", "Projekt ikke fundet med ID: 1"));
    }

    @Test
    void testGenerateProjectReport_GeneralError() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportService.generateProjectReport(eq(1L), any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/reports/project/1")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("error", "Der opstod en fejl ved generering af rapporten: Database error"));
    }

    @Test
    void testGenerateProjectReport_WithoutAuthentication() throws Exception {
        // Act & Assert - Try to access without authentication
        mockMvc.perform(get("/reports/project/1"))
                .andExpect(status().is3xxRedirection());
    }
}