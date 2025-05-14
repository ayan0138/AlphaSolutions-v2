package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubProjectRepository subProjectRepository;

    public TaskService(TaskRepository taskRepository, SubProjectRepository subprojectRepository) {
        this.taskRepository = taskRepository;
        this.subProjectRepository = subprojectRepository;
    }
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findTasksByProjectId(projectId);
    }

    public void createTask(Task task, Long projectId) {
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Opgavens navn er påkrævet");
        }

        if (task.getSubProjectId() == null) {
            throw new IllegalArgumentException("Subprojekt ID er påkrævet");
        }

        if (!subProjectRepository.existsByIdAndProjectId(task.getSubProjectId(), projectId)) {
            throw new IllegalArgumentException("Subprojektet tilhører ikke det angivne projekt");
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

        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        // 💡 Beregn pris
        double total = task.getEstimatedHours() * task.getHourlyRate();
        task.setPrice(java.math.BigDecimal.valueOf(total));

        taskRepository.save(task);
    }

    // En metode, der beregner totalprisen for et subproject
    // ved at summere price for alle dets opgaver (tasks).
    public BigDecimal calculateTotalPriceForSubProject(Long subProjectId) {
        return taskRepository.findBySubProjectId(subProjectId).stream()
                .map(task -> task.getPrice() != null ? task.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Task> getTasksBySubProjectId(Long subProjectId) {
        return taskRepository.findBySubProjectId(subProjectId);
    }

}
