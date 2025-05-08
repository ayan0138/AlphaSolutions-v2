package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping("/my-projects")
    public String showMyProjects(HttpSession session, Model model) {
        long userId = (long) session.getAttribute("userID"); // Sørg for userID sættes ved login

        // Get the logged-in user and add to model
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        List<Project> projects = projectService.getProjectsByUserId(userId);
        model.addAttribute("projects", projects);
        // "projects" skal matche HTML-loop
        return "project-list"; // Navn på .html side i templates = project-list.html
    }

    @GetMapping("/projects/{id}")
    public String showProjectDetails(@PathVariable("id") Long projectId, HttpSession session, Model model) {
        Optional<Project> project = projectService.getProjectById(projectId);

        // Get the logged-in user and add to model
        long userId = (long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        if (project.isPresent()) {
            model.addAttribute("project", project.get());
            return "project-details";
        } else {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }
    }
}