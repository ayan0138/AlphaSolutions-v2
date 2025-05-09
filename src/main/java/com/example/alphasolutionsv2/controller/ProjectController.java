package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

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
        // Get the logged-in user and add to model
        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        Optional<Project> projectOpt = projectService.getProjectById(projectId);

        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();

            // Check if user has access to this project
            if (loggedInUser != null &&
                    (project.getCreatedBy().getUserId() == loggedInUser.getUserId() ||
                            "Admin".equals(loggedInUser.getRole().getRoleName()) ||
                            projectService.userCanViewProject(userId, projectId))) {

                model.addAttribute("project", project);
                return "project-details";
            } else {
                return "redirect:/my-projects?error=Ikke+tilladelse+til+at+se+dette+projekt";
            }
        } else {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }
    }

    // Fixed GET method to show edit form
    @GetMapping("/projects/{id}/edit")
    public String showEditProjectForm(@PathVariable("id") Long projectId, HttpSession session, Model model) {
        // Get the logged-in user
        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);
        model.addAttribute("loggedInUser", loggedInUser);

        Optional<Project> projectOpt = projectService.getProjectById(projectId);

        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();

            // Check if user is authorized to edit
            if (loggedInUser != null &&
                    (project.getCreatedBy().getUserId() == loggedInUser.getUserId() ||
                            "Admin".equals(loggedInUser.getRole().getRoleName()) ||
                            "Projektleder".equals(loggedInUser.getRole().getRoleName()))) {


                model.addAttribute("project", project);
                return "edit-project";
            } else {
                return "redirect:/my-projects?error=Ikke+tilladelse+til+at+redigere";
            }
        } else {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }
    }

    // Fixed POST method to handle form submission
    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable("id") Long projectId,
                                @RequestParam("name") String name,
                                @RequestParam("description") String description,
                                @RequestParam("startDate") String startDateStr,
                                @RequestParam("endDate") String endDateStr,
                                @RequestParam("createdBy.userId") Long createdByUserId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        // Get the logged-in user
        Long userId = (Long) session.getAttribute("userID");
        User loggedInUser = userService.getUserById(userId).orElse(null);

        if (loggedInUser == null) {
            return "redirect:/login?error=Du+skal+være+logget+ind";
        }

        // Get the existing project
        Optional<Project> existingProjectOpt = projectService.getProjectById(projectId);

        if (existingProjectOpt.isEmpty()) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project existingProject = existingProjectOpt.get();

        // Check if user is authorized to edit
        if (existingProject.getCreatedBy().getUserId() != loggedInUser.getUserId() &&
                !"Admin".equals(loggedInUser.getRole().getRoleName()) &&
                !"Projektleder".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/projects/" + projectId + "?error=Ikke+tilladelse+til+at+redigere";
        }


        try {
            // Parse dates
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            // Validate dates
            if (endDate.isBefore(startDate)) {
                model.addAttribute("project", existingProject);
                model.addAttribute("loggedInUser", loggedInUser);
                model.addAttribute("error", "Slutdato skal være efter startdato");
                return "edit-project";
            }

            // Update the existing project
            existingProject.setName(name);
            existingProject.setDescription(description);
            existingProject.setStartDate(startDate);
            existingProject.setEndDate(endDate);

            // Save the updated project
            boolean updated = projectService.updateProject(existingProject);

            if (updated) {
                return "redirect:/projects/" + projectId + "?success=Projekt+opdateret";
            } else {
                model.addAttribute("project", existingProject);
                model.addAttribute("loggedInUser", loggedInUser);
                model.addAttribute("error", "Kunne ikke opdatere projektet");
                return "edit-project";
            }
        } catch (Exception e) {
            model.addAttribute("project", existingProject);
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("error", "Der opstod en fejl: " + e.getMessage());
            return "edit-project";
        }
    }
}
