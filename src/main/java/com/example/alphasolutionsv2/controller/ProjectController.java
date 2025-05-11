package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Optional<User> userOpt = prepareLoggedInUser(session, model);
        if (userOpt.isEmpty()) return "redirect:/login";

        User loggedInUser = userOpt.get();
        List<Project> projects = projectService.getProjectsByUserId(loggedInUser.getUserId());
        model.addAttribute("projects", projects);
        return "my-projects";
    }

    @GetMapping("/projects/{id}")
    public String showProjectDetails(@PathVariable("id") Long projectId, HttpSession session, Model model) {
        Optional<User> userOpt = prepareLoggedInUser(session, model);
        if (userOpt.isEmpty()) return "redirect:/login";

        User loggedInUser = userOpt.get();
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();
        if (project.getCreatedBy().getUserId().equals(loggedInUser.getUserId()) ||
                "Admin".equalsIgnoreCase(loggedInUser.getRole().getRoleName()) ||
                projectService.userCanViewProject(loggedInUser.getUserId(), projectId)) {

            model.addAttribute("project", project);
            return "project-details";
        }

        return "redirect:/my-projects?error=Ingen+rettigheder";
    }

    @GetMapping("/projects/{id}/edit")
    public String showEditProjectForm(@PathVariable("id") Long projectId, HttpSession session, Model model) {
        Optional<User> userOpt = prepareLoggedInUser(session, model);
        if (userOpt.isEmpty()) return "redirect:/login";

        User loggedInUser = userOpt.get();
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();
        if (!projectService.userCanEditProject(loggedInUser, project)) {
            return "redirect:/my-projects?error=Ikke+tilladelse+til+at+redigere";
        }

        model.addAttribute("project", project);
        return "edit-project";
    }

    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable("id") Long projectId,
                                @RequestParam("name") String name,
                                @RequestParam("description") String description,
                                @RequestParam("startDate") String startDateStr,
                                @RequestParam("endDate") String endDateStr,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = prepareLoggedInUser(session, model);
        if (userOpt.isEmpty()) return "redirect:/login";

        User loggedInUser = userOpt.get();
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projekt ikke fundet");
            return "redirect:/my-projects";
        }

        Project project = projectOpt.get();
        if (!projectService.userCanEditProject(loggedInUser, project)) {
            redirectAttributes.addFlashAttribute("error", "Ingen rettigheder til at redigere projektet");
            return "redirect:/projects/" + projectId;
        }

        String errorMessage = projectService.updateProjectDetails(project, name, description, startDateStr, endDateStr);
        if (errorMessage != null) {
            model.addAttribute("project", project);
            model.addAttribute("error", errorMessage);
            return "edit-project";
        }

        redirectAttributes.addFlashAttribute("success", "Projekt opdateret!");
        return "redirect:/projects/" + projectId;
    }

    //  Genanvendelig metode til login-tjek + brugerhentning
    private Optional<User> prepareLoggedInUser(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userID");
        if (userId == null) return Optional.empty();

        User user = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", user);
        return Optional.ofNullable(user);
    }
}
