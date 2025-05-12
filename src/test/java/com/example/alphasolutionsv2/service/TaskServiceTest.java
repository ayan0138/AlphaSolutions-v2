package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private SubProjectRepository subProjectRepository;
    private TaskService taskService;

    @BeforeEach // Setup runs before every test method
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        subProjectRepository = mock(SubProjectRepository.class);
        taskService = new TaskService(taskRepository, subProjectRepository);
    }

    @Test
    void testShouldCreateTaskSuccessfully() {
        Task task = new Task();
        task.setName("Test Opgave");
        task.setSubProjectId(1L);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setEstimatedHours(10.0);
        task.setHourlyRate(500.0);

        when(subProjectRepository.existsByIdAndProjectId(1L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> taskService.createTask(task, 1L));
        verify(taskRepository, times(1)).save(any(Task.class));
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
}
