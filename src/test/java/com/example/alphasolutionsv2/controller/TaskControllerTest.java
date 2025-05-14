package com.example.alphasolutionsv2.controller;


import com.example.alphasolutionsv2.model.Task;

import com.example.alphasolutionsv2.repository.TaskRepository;
import com.example.alphasolutionsv2.service.TaskService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class TaskControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SubProjectService subProjectService;

    // Only mock the AuthenticationManager since it's used for password verification
    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void testShouldSaveAndFetchTask() {
        System.out.println("=== STARTING TEST: testShouldSaveAndFetchTask ===");
        Task task = new Task();
        task.setName("Integration Opgave");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);

        // ----------- ACT -----------
        taskService.createTask(task, 1L);
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);

        // ----------- ASSERT -----------
        assertFalse(tasks.isEmpty(), "Listen af opgaver bør ikke være tom");
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Integration Opgave")),
                "Opgaven med det forventede navn burde være til stede");
        System.out.println("=== TEST COMPLETED ===");
    }

    @Test
    void testDeleteTaskWithCorrectPassword() throws Exception {
        // Arrange - Create a real task using the service
        Task task = new Task();
        task.setName("Test Task to Delete");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Get the created task ID (you may need to fetch it)
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task to Delete".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Mock authentication success
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        // Act & Assert
        mockMvc.perform(post("/tasks/" + createdTask.getTaskId() + "/delete")
                        .param("confirmPassword", "correctpassword")
                        .with(user("marcus").roles("USER"))  // Use a user that exists in your test data
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("success", "Opgave er blevet slettet"));
    }

    @Test
    void testDeleteTaskWithIncorrectPassword() throws Exception {
        // Arrange - Create a real task
        Task task = new Task();
        task.setName("Test Task Wrong Password");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Get the created task ID
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task Wrong Password".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Mock authentication failure
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/tasks/" + createdTask.getTaskId() + "/delete")
                        .param("confirmPassword", "wrongpassword")
                        .with(user("marcus").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/" + createdTask.getTaskId() + "/delete"))
                .andExpect(flash().attribute("error", "Forkert adgangskode. Kunne ikke slette opgave."));
    }

    @Test
    void testShowDeleteTaskConfirmation() throws Exception {
        // Arrange - Create a real task
        Task task = new Task();
        task.setName("Test Task for Confirmation");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Get the created task ID
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task for Confirmation".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Act & Assert
        mockMvc.perform(get("/tasks/" + createdTask.getTaskId() + "/delete")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("Delete-task"))
                .andExpect(model().attributeExists("task"));
    }

    @Test
    void testUpdateTaskSuccess() throws Exception {
        // Arrange - Create a real task
        Task task = new Task();
        task.setName("Test Task to Update");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Get the created task ID
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task to Update".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Act & Assert
        mockMvc.perform(post("/tasks/edit/" + createdTask.getTaskId())
                        .param("taskId", String.valueOf(createdTask.getTaskId()))
                        .param("name", "Updated Task Name")
                        .param("description", "Updated description")
                        .param("estimatedHours", "5.0")
                        .param("hourlyRate", "300.0")
                        .param("dueDate", "2025-05-20")
                        .param("status", "IN_PROGRESS")
                        .param("subProjectId", "1")
                        .param("projectId", "1")
                        .with(user("marcus").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/project/1"));
    }

    @Test
    void testShowEditTaskForm() throws Exception {
        // Arrange - Create a real task
        Task task = new Task();
        task.setName("Test Task to Edit Form");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Get the created task ID
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task to Edit Form".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Act & Assert
        mockMvc.perform(get("/tasks/edit/" + createdTask.getTaskId())
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-task"))
                .andExpect(model().attributeExists("task"));
    }
}