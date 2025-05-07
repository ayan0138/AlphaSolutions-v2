package com.example.alphasolutionsv2.controller;

import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.alphasolutionsv2.service.ProjectService;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Viser formularen til at oprette projekt
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "create-project"; // Matcher HTML-filens navn
    }

    // Modtager og gemmer projektet
    @PostMapping("/create")
    public String handleCreateForm(Model model, Project project) {
        projectService.createProject(project);
        return "redirect:/projects/success"; // Eller tilbage til liste/oversigt
    }

    @GetMapping("")
    public String listProjects(Model model, HttpSession session) {
        // Hent den aktuelle bruger fra session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null) {
            return "redirect:/login"; // Brugeren er ikke logget ind
        }

        Long userId = loggedInUser.getUserId();
        List<Project> projects = projectService.getProjectsByUserId(userId);

        model.addAttribute("projects", projects);
        return "project-list"; // Thymeleaf-fil
    }
}
