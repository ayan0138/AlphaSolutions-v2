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
        // If the task doesn't have a projectId set, use the provided one
        if (task.getProjectId() == null) {
            task.setProjectId(projectId);
        }

        // Call the main createTask method
        createTask(task);
    }

    // Create a task
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

        // Set default values if needed
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("PENDING");
        }

        taskRepository.save(task);
    }
    // Get all tasks
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Get tasks by project ID (if you need this)
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

        // Set default status if needed
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
