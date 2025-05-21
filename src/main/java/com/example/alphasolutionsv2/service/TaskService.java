package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubProjectRepository subProjectRepository;
    private final UserRepository userRepository;
    public TaskService(TaskRepository taskRepository, SubProjectRepository subProjectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.subProjectRepository = subProjectRepository;
        this.userRepository = userRepository;
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
    /**
     * Tildel en opgave til en bruger
     */
    public void assignTaskToUser(long taskId, long userId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Opgave ikke fundet");
        }

        // Tjek om brugeren er en medarbejder
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Bruger ikke fundet");
        }

        // Tjek om brugeren er en medarbejder
        if (!"Medarbejder".equals(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Kun medarbejdere kan tildeles opgaver");
        }

        task.setAssignedToId(userId);
        task.setAssignedUser(user);
        taskRepository.update(task);
    }

    /**
     * Fjern tildeling af en opgave
     */
    public void unassignTask(long taskId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Opgave ikke fundet");
        }

        task.setAssignedToId(null);
        task.setAssignedUser(null);
        taskRepository.update(task);
    }
    /**
     * Hent projekt-ID'et for en opgave
     */
    public Long getProjectIdForTask(long taskId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Opgave ikke fundet");
        }

        // Hvis opgaven har et projekt-ID, returnér det
        if (task.getProjectId() != null) {
            return task.getProjectId();
        }

        // Ellers find projekt-ID'et via subprojektet
        if (task.getSubProjectId() != null) {
            return subProjectRepository.getProjectIdBySubProjectId(task.getSubProjectId());
        }

        return null;
    }
    // Add this method to TaskService class
    public List<Task> getTasksByAssignedUserId(long userId) {
        return taskRepository.findTasksByAssignedUserId(userId);
    }
}

