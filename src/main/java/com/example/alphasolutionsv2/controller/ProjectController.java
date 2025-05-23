package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.UserService;
import com.example.alphasolutionsv2.service.TaskService; // Add this import

import com.example.alphasolutionsv2.model.Project;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final TaskService taskService;
    private final SubProjectService subProjectService;
    private final AuthenticationManager authenticationManager;

    public ProjectController(ProjectService projectService,
                             UserService userService,
                             TaskService taskService,
                             SubProjectService subProjectService,
                             AuthenticationManager authenticationManager) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService;
        this.subProjectService = subProjectService;
        this.authenticationManager = authenticationManager;
    }

    private User loadUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByUsername(userDetails.getUsername()).orElse(null);
    }

    private boolean verifyUserPassword(String username, String password) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication result = authenticationManager.authenticate(authRequest);
            return result.isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @GetMapping("/my-projects")
    public String showMyProjects(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("projects", projectService.getProjectsByUserId(loggedInUser.getUserId()));
        return "my-projects";
    }

    @GetMapping("/projects/{id}")
    public String showProjectDetails(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(id);
        if (projectOpt.isEmpty()) return "redirect:/my-projects?error=Projekt+ikke+fundet";

        Project project = projectOpt.get();
        if (project.getCreatedBy().getUserId().equals(user.getUserId()) ||
                "ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) ||
                projectService.userCanViewProject(user.getUserId(), id)) {

            model.addAttribute("project", project);
            model.addAttribute("loggedInUser", user);
            model.addAttribute("tasks", taskService.getTasksByProjectId(id));
            model.addAttribute("subProjects", subProjectService.getSubProjectsByProjectId(id));

            return "project-details";
        }

        return "redirect:/my-projects?error=Ingen+rettigheder";
    }

    @GetMapping("/projects/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(id);
        if (projectOpt.isEmpty()) return "redirect:/my-projects?error=Projekt+ikke+fundet";

        Project project = projectOpt.get();
        if (!projectService.userCanEditProject(user, project)) {
            return "redirect:/my-projects?error=Ikke+tilladt";
        }

        model.addAttribute("project", project);
        model.addAttribute("loggedInUser", user);
        return "edit-project";
    }

    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String description,
                                @RequestParam String startDateStr,
                                @RequestParam String endDateStr,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(id);
        if (projectOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Projekt ikke fundet");
            return "redirect:/my-projects";
        }

        Project project = projectOpt.get();
        if (!projectService.userCanEditProject(user, project)) {
            redirectAttributes.addFlashAttribute("error", "Ingen rettigheder til at redigere");
            return "redirect:/projects/" + id;
        }

        String error = projectService.updateProjectDetails(project, name, description, startDateStr, endDateStr);
        if (error != null) {
            model.addAttribute("project", project);
            model.addAttribute("error", error);
            model.addAttribute("loggedInUser", user);
            return "edit-project";
        }

        redirectAttributes.addFlashAttribute("success", "Projekt opdateret!");
        return "redirect:/projects/" + id;
    }

    @GetMapping("/projects/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Project project = new Project();
        project.setCreatedBy(user);

        model.addAttribute("project", project);
        model.addAttribute("loggedInUser", user);
        return "create-project";
    }

    @PostMapping("/projects/create")
    public String createProject(@ModelAttribute Project project,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        project.setCreatedBy(user);

        try {
            projectService.createProject(project);
            redirectAttributes.addFlashAttribute("success", "Projekt oprettet!");
            return "redirect:/my-projects";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/projects/create";
        }
    }

    @GetMapping("/projects/{id}/delete")
    public String showDeleteConfirmation(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(id);
        if (projectOpt.isEmpty()) return "redirect:/my-projects?error=Projekt+ikke+fundet";

        Project project = projectOpt.get();
        if (!projectService.userCanDeleteProject(user, project)) {
            return "redirect:/my-projects?error=Ingen+tilladelse";
        }

        model.addAttribute("project", project);
        model.addAttribute("loggedInUser", user);
        return "delete-project-confirmation"; // TODO: Lav denne HTML-side
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id,
                                @RequestParam String confirmPassword,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        if (!verifyUserPassword(user.getUsername(), confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Forkert adgangskode");
            return "redirect:/projects/" + id + "/edit-project";
        }

        boolean deleted = projectService.deleteProject(id, user);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Projekt slettet");
        } else {
            redirectAttributes.addFlashAttribute("error", "Kunne ikke slette projekt");
        }

        return "redirect:/my-projects";
    }

    @GetMapping("/projects/{projectId}/subprojects/create")
    public String showCreateSubprojectForm(@PathVariable long projectId,
                                           @AuthenticationPrincipal UserDetails userDetails,
                                           Model model) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) return "redirect:/my-projects?error=Projekt+ikke+fundet";

        SubProject subProject = new SubProject();
        subProject.setProjectId(projectId);
        model.addAttribute("subProject", subProject);
        model.addAttribute("project", projectOpt.get());
        model.addAttribute("loggedInUser", user);
        return "create-subproject";
    }

    @PostMapping("/projects/{projectId}/subprojects/create")
    public String createSubproject(@PathVariable long projectId,
                                   @ModelAttribute SubProject subProject,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = loadUser(userDetails);
        if (user == null) return "redirect:/login";

        try {
            subProject.setProjectId(projectId);
            subProject.setCreatedAt(LocalDateTime.now());

            SubProject created = subProjectService.createSubProject(subProject);
            redirectAttributes.addFlashAttribute("success", "Subprojekt oprettet!");
            return "redirect:/projects/" + projectId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("subProject", subProject);
            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            projectOpt.ifPresent(p -> model.addAttribute("project", p));
            model.addAttribute("loggedInUser", user);
            return "create-subproject";
        }
    }
}