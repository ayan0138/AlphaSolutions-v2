package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private SubProjectRepository subProjectRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        subProjectRepository = mock(SubProjectRepository.class);
        taskService = new TaskService(taskRepository, subProjectRepository);
    }

    @Test
    void testShouldCreateTaskSuccessfully() {
        // Arrange
        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setDueDate(LocalDate.now().plusDays(7)); // Add this line!
        task.setEstimatedHours(5.0);
        task.setHourlyRate(100.0);
        task.setSubProjectId(1L);

        // Act
        taskService.createTask(task);

        // Assert
        verify(taskRepository).save(task);
        assertEquals(LocalDateTime.now().getDayOfYear(), task.getCreatedAt().getDayOfYear());
        assertEquals("PENDING", task.getStatus());
    }

    @Test
    void testShouldThrowArgumentIfTheresNoName() {

        Task task = new Task();
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setEstimatedHours(5.0);
        task.setHourlyRate(300.0);

        //assert
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 1L));
        assertTrue(ex.getMessage().contains("navn"));
    }
    @Test
    void testDeleteTask() {
        // Arrange
        long taskId = 1L;

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void testUpdateTask() {
        // Arrange
        Task task = new Task();
        task.setTaskId(1L);
        task.setName("Updated Task");
        task.setDescription("Updated description");
        task.setEstimatedHours(5.0);
        task.setHourlyRate(300.0);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setSubProjectId(1L);
        task.setStatus("IN_PROGRESS");

        // Act
        taskService.updateTask(task);

        // Assert
        verify(taskRepository).update(task);
    }

    @Test
    void testUpdateTaskWithInvalidData() {
        // Arrange
        Task task = new Task();
        task.setTaskId(1L);
        task.setName(""); // Invalid empty name

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(task));
    }
}
