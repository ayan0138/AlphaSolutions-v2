package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    //  Positiv test: Gem opgave korrekt
    @Test
    void createTask_shouldSaveTask_whenValidInput() {
        Task task = new Task();
        task.setName("Frontend opgave");
        task.setSubProjectId(1L);
        task.setEstimatedHours(5.0);
        task.setHourlyRate(300.0);
        task.setDueDate(LocalDate.now().plusDays(3));

        when(subProjectRepository.existsByIdAndProjectId(1L, 10L)).thenReturn(true);

        taskService.createTask(task, 10L);

        verify(taskRepository, times(1)).save(task);
        assertNotNull(task.getCreatedAt());
    }

    //  Negativ test: Opgavenavn mangler
    @Test
    void createTask_shouldThrow_whenNameIsMissing() {
        Task task = new Task();
        task.setSubProjectId(1L);
        task.setEstimatedHours(4.0);
        task.setHourlyRate(250.0);
        task.setDueDate(LocalDate.now().plusDays(2));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 10L));

        assertEquals("Opgavens navn er påkrævet", ex.getMessage());
    }

    //  Negativ test: Subprojekt ID er null
    @Test
    void createTask_shouldThrow_whenSubProjectIdIsNull() {
        Task task = new Task();
        task.setName("Databaseopgave");
        task.setEstimatedHours(6.0);
        task.setHourlyRate(320.0);
        task.setDueDate(LocalDate.now().plusDays(1));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 10L));

        assertEquals("Subprojekt ID er påkrævet", ex.getMessage());
    }

    // Negativ test: Subprojekt matcher ikke projektet
    @Test
    void createTask_shouldThrow_whenSubProjectDoesNotBelongToProject() {
        Task task = new Task();
        task.setName("Sikkerhedsopgave");
        task.setSubProjectId(2L);
        task.setEstimatedHours(7.0);
        task.setHourlyRate(300.0);
        task.setDueDate(LocalDate.now().plusDays(5));

        when(subProjectRepository.existsByIdAndProjectId(2L, 10L)).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 10L));

        assertEquals("Subprojektet tilhører ikke det angivne projekt", ex.getMessage());
    }

    // Positiv test: Hent tasks for projekt
    @Test
    void getTasksByProjectId_shouldReturnListFromRepository() {
        when(taskRepository.findTasksByProjectId(1L)).thenReturn(List.of(new Task()));

        List<Task> tasks = taskService.getTasksByProjectId(1L);

        assertEquals(1, tasks.size());
        verify(taskRepository, times(1)).findTasksByProjectId(1L);
    }

    @Test
    void createTask_shouldThrow_whenEstimatedHoursIsZero() {
        Task task = new Task();
        task.setName("Fejl-opgave");
        task.setSubProjectId(1L);
        task.setEstimatedHours(0.0);
        task.setHourlyRate(250.0);
        task.setDueDate(LocalDate.now().plusDays(2));

        when(subProjectRepository.existsByIdAndProjectId(1L, 1L)).thenReturn(true); // FIX

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 1L));

        assertEquals("Estimeret tid for opgaven er påkrævet", ex.getMessage());
    }

    @Test
    void createTask_shouldThrow_whenHourlyRateIsZero() {
        Task task = new Task();
        task.setName("Fejl-opgave");
        task.setSubProjectId(1L);
        task.setEstimatedHours(5.0);
        task.setHourlyRate(0.0);
        task.setDueDate(LocalDate.now().plusDays(2));

        when(subProjectRepository.existsByIdAndProjectId(1L, 1L)).thenReturn(true); // 🛠️ Tilføj dette!

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(task, 1L));

        assertEquals("Timepris for opgaven er påkrævet", ex.getMessage());
    }

}
