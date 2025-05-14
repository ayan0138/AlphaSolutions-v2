package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/project/{projectId}") //via udvalgte projekt og projekt id, fremvises oprettet projekt
    public String getTasksByProject(@PathVariable long projectId, Model model) {
        model.addAttribute("tasks", taskService.getTasksByProjectId(projectId));
        model.addAttribute("projectId", projectId);
        return "tasks/project-tasks"; // henviser til project-task HTML
    }

    @GetMapping("/project/{projectId}/tasks") //via udvalgte projekt og projekt id, fremvises oprettet projekt + tildelte opgaver
    public String showTasksUnderProject(@PathVariable long projectId, Model model) {
        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        return "tasks/tasks-list"; // henviser til task-list HTMl
    }

    @PostMapping("/create")
    public String createTask(@RequestParam String name,
                             @RequestParam Long projectId,
                             @RequestParam Long subProjectId,
                             @RequestParam Double estimatedHours,
                             @RequestParam Double hourlyRate,
                             @RequestParam LocalDate dueDate) {

        Task task = new Task();
        task.setName(name);
        task.setSubProjectId(subProjectId);
        task.setEstimatedHours(estimatedHours);
        task.setHourlyRate(hourlyRate);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.now());

        try {
            taskService.createTask(task, projectId); // <-- korrekt parameter
            return "redirect:/tasks/project/" + projectId + "?success=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/tasks/project/" + projectId + "?error=" + URLEncoder.encode(e.getMessage(),
                    StandardCharsets.UTF_8);
        }
    }
}
