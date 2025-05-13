/* ORIGINAL
package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/create")
    public String showCreateTaskForm(Model model) {
        model.addAttribute("task", new Task());
        return "create-task"; // Thymeleaf-side: src/main/resources/templates/create-task.html
    }

    @PostMapping("/create")
    public String createTask(@ModelAttribute Task task, HttpSession session) {
        taskService.createTask(task);
        return "redirect:/my-projects/" + task.getProjectId();
    }
    @PostMapping("/projects/{projectId}/tasks")
    public String createTask(@PathVariable("projectId") Long projectId,
                             @ModelAttribute Task task,
                             RedirectAttributes redirectAttributes) {
        try {
            task.setSubProjectId(projectId);  // Sørg for at opgaven er knyttet til det rigtige projekt
            taskService.createTask(task);
            redirectAttributes.addFlashAttribute("success", "Opgave oprettet!");
            return "redirect:/projects/{projectId}/tasks";  // Redirect til liste af opgaver
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/projects/{projectId}/tasks/create";  // Hvis fejl, kom tilbage til formularen
        }
    }
}
*/


//SENESTE TASKCONTROLLER 13 maj kl 14.03
/*
package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/project/{projectId}/create-task")
    public String showCreateTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("task", new Task());
        return "create-task"; // HTML-skabelon til at oprette en opgave
    }
    @PostMapping("/project/{projectId}/create-task")
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
            taskService.createTask(task, projectId); // korrekt projektId sendes med
            return "redirect:/tasks/project/" + projectId + "/tasks";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("projectId", projectId);
            model.addAttribute("task", task);
            return "create-task";
        }}
}

    /*
    public String createTask(@RequestParam String name,
                             @RequestParam Long subProjectId,
                             @RequestParam Double estimatedHours,
                             @RequestParam Double hourlyRate,
                             @RequestParam LocalDate dueDate) {

        Task task = new Task();
        task.setName(name);
        task.setSubProjectId(subProjectId);
        task.setEstimatedHours(estimatedHours);
        task.setHourlyRate(hourlyRate);
        task.setDueDate(LocalDate.from(dueDate));
        task.setCreatedAt(LocalDateTime.now());

        try {
            taskService.createTask(task, subProjectId);
            return "redirect:/create-task";
        } catch (IllegalArgumentException e) {
            return "redirect:/tasks/error";
        }
    }
} */

