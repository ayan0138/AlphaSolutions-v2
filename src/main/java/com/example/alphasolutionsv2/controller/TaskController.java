package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import com.example.alphasolutionsv2.service.TaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final SubProjectRepository subProjectRepository;

    public TaskController(TaskService taskService, SubProjectRepository subProjectRepository) {
        this.taskService = taskService;
        this.subProjectRepository = subProjectRepository;
    }
    @GetMapping("/create") // opretter task til 2.1
    public String showTaskProjectForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("projectId", projectId);
        return "create-task";
    }

    @PostMapping("/create-task")
    public String createTask(@PathVariable Long projectId,
                             @RequestParam String name,
                             @RequestParam Long subProjectId,
                             @RequestParam Double estimatedHours,
                             @RequestParam Double hourlyRate,
                             @RequestParam LocalDate dueDate,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        Task task = new Task();
        task.setName(name);
        task.setSubProjectId(subProjectId);
        task.setEstimatedHours(estimatedHours);
        task.setHourlyRate(hourlyRate);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.now());

        try {
            taskService.createTask(task, projectId);
            redirectAttributes.addFlashAttribute("success", "Opgaven blev oprettet!");
            return "redirect:/projects/" + projectId + "/tasks/create"; // korrekt redirect med projektId
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("task", task);
            model.addAttribute("projectId", projectId);
            return "create-task";
        }
    }


// 2.6 - Hent alle opgaver for et projekt
    @GetMapping
    public String getTasksByProject(@PathVariable Long projectId, Model model) {
        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        return "tasks-list"; // HTML-side med liste over opgaver
    }

    // 2.3 + 2.4 - Vis formular til oprettelse af opgave
    @GetMapping("/new")
    public String showCreateTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("projectId", projectId);
        return "tasks/create-task"; // Formular til opgaveoprettelse
    }

    // 2.1 + 2.2 + 2.5 - Håndter oprettelse af opgave
    @PostMapping("/create")
    public String createTask(@PathVariable Long projectId,
                             @RequestParam String name,
                             @RequestParam Long subProjectId,
                             @RequestParam Double estimatedHours,
                             @RequestParam Double hourlyRate,
                             @RequestParam LocalDate dueDate,
                             Model model) {

        Task task = new Task();
        task.setName(name);
        task.setSubProjectId(subProjectId);
        task.setEstimatedHours(estimatedHours);
        task.setHourlyRate(hourlyRate);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.now());

        try {
            taskService.createTask(task, projectId);
            return "redirect:/projects/" + projectId + "/create-task"; //viser subtask under projekt
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("task", task);
            model.addAttribute("projectId", projectId);
            return "redirect:/projects/" + projectId + "/tasks";
        }
    }
}
