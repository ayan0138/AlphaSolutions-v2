package controller;

import jakarta.servlet.http.HttpSession;
import model.Project;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import service.ProjectService;

import java.util.List;
import org.springframework.stereotype.Controller;

@Controller
public class ProjectController {

    private final ProjectService projectService;

    // Constructor injection
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/my-projects")
    public String showMyProjects(HttpSession session, Model model) {
        long userId = (long) session.getAttribute("userID"); // Sørg for userID sættes ved login
        List<Project> projects = projectService.getProjectsByUser(userId);
        model.addAttribute("projects", projects); // "projects" skal matche HTML-loop
        return "project-list"; // Navn på  .html side i templates = project-list.html
    }
    }


