package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/create")
    public String createTask(@RequestParam String name,
                             @RequestParam Long subProjectId,
                             @RequestParam Double estimatedHours,
                             @RequestParam Double hourlyRate,
                             @RequestParam LocalDateTime dueDate) {

        Task task = new Task();
        task.setName(name);
        task.setSubProjectId(subProjectId);
        task.setEstimatedHours(estimatedHours);
        task.setHourlyRate(hourlyRate);
        task.setDueDate(LocalDate.from(dueDate));
        task.setCreatedAt(LocalDateTime.now());

        try {
            taskService.createTask(task, subProjectId);
            return "redirect:/tasks/success";
        } catch (IllegalArgumentException e) {
            return "redirect:/tasks/error";
        }
    }
}
