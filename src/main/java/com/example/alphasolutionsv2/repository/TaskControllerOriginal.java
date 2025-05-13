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