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
        Long userId = (Long) session.getAttribute("userID"); // Sørg for userID sættes ved login

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
        // Get the logged-in user and add to model
        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        Optional<Project> projectOpt = projectService.getProjectById(projectId);

        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();

            // Check if user has access to this project
            if (loggedInUser != null &&
                    (project.getCreatedBy().getUserId().equals(loggedInUser.getUserId()) ||
                            "Admin".equals(loggedInUser.getRole().getRoleName()) ||
                            projectService.userCanViewProject(userId, projectId))) {

                model.addAttribute("project", project);
                return "project-details";
            }
            else {
                return "redirect:/my-projects?error=Ikke+tilladelse+til+at+se+dette+projekt";
            }
        } else {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }
    }

    // Fixed GET method to show edit form
    @GetMapping("/projects/{id}/edit")
    public String showEditProjectForm(@PathVariable("id") Long projectId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

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


    // Fixed POST method to handle form submission
    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable("id") Long projectId,
                                @RequestParam("name") String name,
                                @RequestParam("description") String description,
                                @RequestParam("startDate") String startDateStr,
                                @RequestParam("endDate") String endDateStr,
                                HttpSession session,
                                Model model, RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty() || loggedInUser == null) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet+eller+login+påkrævet";
        }

        Project project = projectOpt.get();

        if (!projectService.userCanEditProject(loggedInUser, project)) {
            redirectAttributes.addFlashAttribute("error", "Ikke rettigheder til at redigere");
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
}
