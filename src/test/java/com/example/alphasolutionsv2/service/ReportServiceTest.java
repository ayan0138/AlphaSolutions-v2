package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private SubProjectService subProjectService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ReportService reportService;

    private Project testProject;
    private User testUser;
    private SubProject testSubProject;
    private Task testTask1;
    private Task testTask2;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        // Setup test project
        testProject = new Project();
        testProject.setProjectId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStartDate(LocalDate.now());
        testProject.setEndDate(LocalDate.now().plusDays(30));
        testProject.setCreatedBy(testUser);

        // Setup test subproject
        testSubProject = new SubProject();
        testSubProject.setSubProjectId(1L);
        testSubProject.setName("Test SubProject");
        testSubProject.setProjectId(1L);

        // Setup test tasks
        testTask1 = new Task();
        testTask1.setTaskId(1L);
        testTask1.setName("Task 1");
        testTask1.setDescription("Task 1 Description");
        testTask1.setEstimatedHours(8.0);
        testTask1.setHourlyRate(500.0);
        testTask1.setStatus("PENDING");
        testTask1.setSubProjectId(1L);
        testTask1.setDueDate(LocalDate.now().plusDays(7));

        testTask2 = new Task();
        testTask2.setTaskId(2L);
        testTask2.setName("Task 2");
        testTask2.setDescription("Task 2 Description");
        testTask2.setEstimatedHours(12.0);
        testTask2.setHourlyRate(400.0);
        testTask2.setStatus("IN_PROGRESS");
        testTask2.setSubProjectId(1L);
        testTask2.setDueDate(LocalDate.now().plusDays(14));
    }

    @Test
    void testGetProjectReportData_Success() {
        // Arrange
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(testProject));
        when(subProjectService.getSubProjectsByProjectId(1L)).thenReturn(List.of(testSubProject));
        when(taskService.getTasksByProjectId(1L)).thenReturn(List.of(testTask1, testTask2));
        when(subProjectService.getSubProjectById(1L)).thenReturn(testSubProject);

        // Act
        ReportService.ProjectReportData result = reportService.getProjectReportData(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProject, result.getProject());
        assertEquals(1, result.getSubProjects().size());
        assertEquals(2, result.getTasks().size());

        // Verify that subproject info was added to tasks
        assertEquals(testSubProject, result.getTasks().get(0).getSubProject());
        assertEquals(testSubProject, result.getTasks().get(1).getSubProject());
    }

    @Test
    void testGetProjectReportData_ProjectNotFound() {
        // Arrange
        when(projectService.getProjectById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reportService.getProjectReportData(1L));
    }

    @Test
    void testGenerateProjectReport_Success() {
        // Arrange
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(testProject));
        when(subProjectService.getSubProjectsByProjectId(1L)).thenReturn(List.of(testSubProject));
        when(taskService.getTasksByProjectId(1L)).thenReturn(List.of(testTask1, testTask2));
        when(subProjectService.getSubProjectById(1L)).thenReturn(testSubProject);

        // Act
        ProjectReport result = reportService.generateProjectReport(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testProject, result.getProject());
        assertEquals(testUser, result.getGeneratedBy());
        assertNotNull(result.getGeneratedAt());
        assertNotNull(result.getSummary());

        // Verify summary calculations
        ReportSummary summary = result.getSummary();
        assertEquals(20.0, summary.getTotalEstimatedHours()); // 8.0 + 12.0
        assertEquals(8800.0, summary.getTotalCost()); // (8*500) + (12*400)
        assertEquals(2, summary.getTotalTasks());

        // Verify status breakdown
        assertEquals(1, summary.getTasksByStatus().get("PENDING"));
        assertEquals(1, summary.getTasksByStatus().get("IN_PROGRESS"));
    }

    @Test
    void testGenerateProjectReport_WithNullValues() {
        // Arrange - Create task with null values
        Task taskWithNulls = new Task();
        taskWithNulls.setTaskId(3L);
        taskWithNulls.setName("Task with nulls");
        taskWithNulls.setEstimatedHours(null);
        taskWithNulls.setHourlyRate(null);
        taskWithNulls.setStatus(null);

        when(projectService.getProjectById(1L)).thenReturn(Optional.of(testProject));
        when(subProjectService.getSubProjectsByProjectId(1L)).thenReturn(List.of(testSubProject));
        when(taskService.getTasksByProjectId(1L)).thenReturn(List.of(testTask1, taskWithNulls));
        when(subProjectService.getSubProjectById(1L)).thenReturn(testSubProject);

        // Act
        ProjectReport result = reportService.generateProjectReport(1L, testUser);

        // Assert
        assertNotNull(result);
        ReportSummary summary = result.getSummary();

        // Should only count task1 in calculations
        assertEquals(8.0, summary.getTotalEstimatedHours());
        assertEquals(4000.0, summary.getTotalCost());
        assertEquals(2, summary.getTotalTasks());

        // Null status should be counted as "UNKNOWN"
        assertEquals(1, summary.getTasksByStatus().get("PENDING"));
        assertEquals(1, summary.getTasksByStatus().get("UNKNOWN"));
    }

    @Test
    void testGenerateProjectReport_TasksWithoutSubProject() {
        // Arrange - Create task without subproject
        Task taskWithoutSubProject = new Task();
        taskWithoutSubProject.setTaskId(3L);
        taskWithoutSubProject.setName("Task without subproject");
        taskWithoutSubProject.setEstimatedHours(5.0);
        taskWithoutSubProject.setHourlyRate(300.0);
        taskWithoutSubProject.setStatus("COMPLETED");
        taskWithoutSubProject.setSubProjectId(null);

        when(projectService.getProjectById(1L)).thenReturn(Optional.of(testProject));
        when(subProjectService.getSubProjectsByProjectId(1L)).thenReturn(List.of(testSubProject));
        when(taskService.getTasksByProjectId(1L)).thenReturn(List.of(testTask1, taskWithoutSubProject));
        when(subProjectService.getSubProjectById(1L)).thenReturn(testSubProject);

        // Act
        ProjectReport result = reportService.generateProjectReport(1L, testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTasksBySubProject().containsKey("Test SubProject"));
        assertTrue(result.getTasksBySubProject().containsKey("Ingen subprojekt"));
        assertEquals(1, result.getTasksBySubProject().get("Test SubProject").size());
        assertEquals(1, result.getTasksBySubProject().get("Ingen subprojekt").size());
    }

    @Test
    void testReportSummaryFormatting() {
        // Arrange
        ReportSummary summary = new ReportSummary(25.5, 12750.75, 3, Map.of());

        // Act & Assert
        String formattedHours = summary.getFormattedTotalHours();
        String formattedCost = summary.getFormattedTotalCost();

        // Check that the formatted strings contain the expected components
        assertTrue(formattedHours.contains("25.5") || formattedHours.contains("25,5"),
                "Expected formatted hours to contain 25.5 or 25,5, but was: " + formattedHours);
        assertTrue(formattedHours.contains("timer"),
                "Expected formatted hours to contain 'timer', but was: " + formattedHours);

        assertTrue(formattedCost.contains("12750") && (formattedCost.contains(".75") || formattedCost.contains(",75")),
                "Expected formatted cost to contain 12750.75 or 12750,75, but was: " + formattedCost);
        assertTrue(formattedCost.contains("DKK"),
                "Expected formatted cost to contain 'DKK', but was: " + formattedCost);
    }
}