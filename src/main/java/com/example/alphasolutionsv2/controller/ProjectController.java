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
        return "projects/my-projects";
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

            return "projects/project-details";
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
        return "projects/edit-project";
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
            return "projects/edit-project";
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
        return "projects/create-project";
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
        return "projects/delete-project-confirmation"; // TODO: Lav denne HTML-side
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
            return "redirect:/projects/" + id + "/edit";
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
        return "subprojects/create-subproject";
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
            return "subprojects/create-subproject";
        }
    }
    // Add these methods to your existing ProjectController class

    @GetMapping("/subprojects/{subProjectId}/edit")
    public String showEditSubProjectForm(@PathVariable long subProjectId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        // Get the subproject
        SubProject subProject = subProjectService.getSubProjectById(subProjectId);
        if (subProject == null) {
            return "redirect:/my-projects?error=Subprojekt+ikke+fundet";
        }

        // Get the parent project for context
        Optional<Project> projectOpt = projectService.getProjectById(subProject.getProjectId());

        model.addAttribute("subProject", subProject);
        model.addAttribute("loggedInUser", loggedInUser);
        projectOpt.ifPresent(project -> model.addAttribute("project", project));

        return "subprojects/edit-subproject";
    }

    @PostMapping("/subprojects/{subProjectId}/edit")
    public String updateSubProject(@PathVariable long subProjectId,
                                   @ModelAttribute SubProject subProject,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        try {
            SubProject existingSubProject = subProjectService.getSubProjectById(subProjectId);
            if (existingSubProject == null) {
                return "redirect:/my-projects?error=Subprojekt+ikke+fundet";
            }

            // Validate dates - check if both dates are provided
            if (subProject.getStartDate() != null && subProject.getEndDate() != null &&
                    subProject.getEndDate().isBefore(subProject.getStartDate())) {
                model.addAttribute("error", "Slutdato kan ikke være før startdato");
                model.addAttribute("subProject", subProject);
                model.addAttribute("loggedInUser", loggedInUser);

                // Get project for display
                Optional<Project> projectOpt = projectService.getProjectById(existingSubProject.getProjectId());
                projectOpt.ifPresent(project -> model.addAttribute("project", project));

                return "subprojects/edit-subproject";
            }

            // Set ID and project ID from existing subproject to ensure they're not modified
            subProject.setSubProjectId(subProjectId);
            subProject.setProjectId(existingSubProject.getProjectId());
            subProject.setCreatedAt(existingSubProject.getCreatedAt()); // Keep original creation time

            // Update the subproject
            subProjectService.updateSubProject(subProject);

            redirectAttributes.addFlashAttribute("success", "Subprojekt opdateret!");
            return "redirect:/projects/" + subProject.getProjectId();
        } catch (Exception e) {
            model.addAttribute("error", "Fejl ved opdatering: " + e.getMessage());
            model.addAttribute("subProject", subProject);
            model.addAttribute("loggedInUser", loggedInUser);

            // Get project for display in case of error
            SubProject existingSubProject = subProjectService.getSubProjectById(subProjectId);
            if (existingSubProject != null) {
                Optional<Project> projectOpt = projectService.getProjectById(existingSubProject.getProjectId());
                projectOpt.ifPresent(project -> model.addAttribute("project", project));
            }

            return "subprojects/edit-subproject";
        }
    }
}
