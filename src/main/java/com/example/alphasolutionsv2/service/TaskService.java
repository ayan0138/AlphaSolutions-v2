package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubProjectRepository subProjectRepository;

    public TaskService(TaskRepository taskRepository, SubProjectRepository subprojectRepository) {
        this.taskRepository = taskRepository;
        this.subProjectRepository = subprojectRepository;
    }

    public void createTask(Task task, long projectId) {
        if (task.getName() == null || task.getName().isEmpty()) {
            throw new IllegalArgumentException("Opgavens navn er påkrævet");
        }

        if (task.getSubProjectId() == null) {
            throw new IllegalArgumentException("Subprojekt ID er påkrævet");
        }
        if (!subProjectRepository.existsByIdAndProjectId(task.getSubProjectId(), projectId)) { //hvis subprojekt ikke findes, under projekt.
            throw new IllegalArgumentException("Subprojektet tilhører ikke det angivne projekt");
        }

        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Deadline for opgaven er påkrævet");
        }
        if (task.getEstimatedHours() == null || task.getEstimatedHours() <= 0) {
            throw new IllegalArgumentException("Estimeret tid for opgaven er påkrævet"); // og skal være større end 0
        }

        if (task.getHourlyRate() == null || task.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Timepris for opgaven er påkrævet"); //og skal være større end 0
        }


        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        taskRepository.save(task);
    }

}
