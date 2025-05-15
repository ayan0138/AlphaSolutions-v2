package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.TaskRepository;
import com.example.alphasolutionsv2.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MockMvc mockMvc;

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
    void testShowAllTasks() throws Exception {
        // Act & Assert - Test showing all tasks
        mockMvc.perform(get("/tasks")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks-list"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testGetTasksByProject() throws Exception {
        // Arrange - Create a test task
        Task task = new Task();
        task.setName("Test Task for Project View");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        // Act & Assert - Test viewing tasks by project
        mockMvc.perform(get("/tasks/project/1")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks-list"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("projectId"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testShowCreateTaskForm() throws Exception {
        // Act & Assert - Test showing the create task form
        mockMvc.perform(get("/tasks/create")
                        .param("projectId", "1")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testShowCreateTaskFormForProject() throws Exception {
        // Act & Assert - Test showing the create task form for a specific project
        mockMvc.perform(get("/tasks/project/1/create")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testCreateTask() throws Exception {
        // Act & Assert - Test creating a new task
        mockMvc.perform(post("/tasks/create")
                        .param("name", "New Task via Form")
                        .param("description", "Task created through form")
                        .param("estimatedHours", "6.0")
                        .param("hourlyRate", "400.0")
                        .param("dueDate", "2025-05-25")
                        .param("status", "PENDING")
                        .param("subProjectId", "1")
                        .param("projectId", "1")
                        .with(user("marcus").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/project/1"));

        // Verify the task was created
        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        assertTrue(tasks.stream().anyMatch(t -> "New Task via Form".equals(t.getName())),
                "Den nye opgave skulle være oprettet");
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
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testUpdateTask() throws Exception {
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
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testShowTasksBySubProject() throws Exception {
        // Act & Assert - Test viewing tasks by subproject (assuming subproject 1 exists)
        mockMvc.perform(get("/tasks/subproject/1")
                        .with(user("marcus").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("subproject-tasks"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("subProject"))
                .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    void testCreateTaskWithoutAuthentication() throws Exception {
        // Act & Assert - Test accessing create task form without authentication
        mockMvc.perform(get("/tasks/create"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testEditTaskWithoutAuthentication() throws Exception {
        // Arrange - Create a task first
        Task task = new Task();
        task.setName("Test Task No Auth");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);
        taskService.createTask(task, 1L);

        List<Task> tasks = taskRepository.findTasksByProjectId(1L);
        Task createdTask = tasks.stream()
                .filter(t -> "Test Task No Auth".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Act & Assert - Test accessing edit task form without authentication
        mockMvc.perform(get("/tasks/edit/" + createdTask.getTaskId()))
                .andExpect(status().is3xxRedirection());
    }
}
