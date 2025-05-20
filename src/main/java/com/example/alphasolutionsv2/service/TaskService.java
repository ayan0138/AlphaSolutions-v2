package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubProjectRepository subProjectRepository;
    public TaskService(TaskRepository taskRepository, SubProjectRepository subProjectRepository) {
        this.taskRepository = taskRepository;
        this.subProjectRepository = subProjectRepository;
    }
    public void createTask(Task task, long projectId) {
        // Hvis task ikke har et projectId angivet, så brug det angivne
        if (task.getProjectId() == null) {
            task.setProjectId(projectId);
        }

        // Kald hovedemetoden createTask
        createTask(task);
    }

    // Opret en task
    public void createTask(Task task) {
        // Validation
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Opgavens navn er påkrævet");
        }

        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Deadline for opgaven er påkrævet");
        }

        if (task.getEstimatedHours() == null || task.getEstimatedHours() <= 0) {
            throw new IllegalArgumentException("Estimeret tid for opgaven er påkrævet");
        }

        if (task.getHourlyRate() == null || task.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Timepris for opgaven er påkrævet");
        }

        // Set default values hvis nødvændigt
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("PENDING");
        }

        taskRepository.save(task);
    }
    // Get alle tasks
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Get tasks efter project ID (hvis du har brug det)
    public List<Task> getTasksByProjectId(long projectId) {
        return taskRepository.findTasksByProjectId(projectId);
    }
    public Task getTaskById(long taskId) {
        return taskRepository.findById(taskId);
    }


    /**
     * Update an existing task
     */
    public void updateTask(Task task) {
        // Validation
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Opgavens navn er påkrævet");
        }

        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Deadline for opgaven er påkrævet");
        }

        if (task.getEstimatedHours() == null || task.getEstimatedHours() <= 0) {
            throw new IllegalArgumentException("Estimeret tid for opgaven er påkrævet");
        }

        if (task.getHourlyRate() == null || task.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Timepris for opgaven er påkrævet");
        }

        if (task.getSubProjectId() == null) {
            throw new IllegalArgumentException("Subprojekt skal vælges");
        }

        // Set default status hvis der er brug for det
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("PENDING");
        }

        taskRepository.update(task);
    }
    public List<Task> getTasksBySubProjectId(long subProjectId) {
        return taskRepository.findTasksBySubProjectId(subProjectId);
    }
    public void deleteTask(long taskId) {
        taskRepository.deleteById(taskId);
    }

}
