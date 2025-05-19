package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.UserService;
import com.example.alphasolutionsv2.service.TaskService; // Add this import

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Task; // Add this import
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Optional;

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final TaskService taskService; // Add this field
    private final SubProjectService subProjectService;
    @Autowired
    private AuthenticationManager authenticationManager;

    // Update constructor to include TaskService
    public ProjectController(ProjectService projectService, UserService userService, TaskService taskService, SubProjectService subProjectService) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService; // Add this
        this.subProjectService = subProjectService;
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
        if(loggedInUser==null){
            return "redirect:/login";
        }

        model.addAttribute("loggedInUser", loggedInUser);
        List<Project> projects = projectService.getProjectsByUserId(loggedInUser.getUserId());
        model.addAttribute("projects", projects);
        return "my-projects";
    }

    @GetMapping("/projects/{id}")
    public String showProjectDetails(@PathVariable("id") Long projectId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User loggedInUser = loadUser(userDetails);
        if(loggedInUser==null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if(projectOpt.isEmpty()){
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();
        if(project.getCreatedBy().getUserId().equals(loggedInUser.getUserId()) ||
                "ADMIN".equalsIgnoreCase(loggedInUser.getRole().getRoleName()) ||
                projectService.userCanViewProject(loggedInUser.getUserId(), projectId)) {

            model.addAttribute("project", project);
            model.addAttribute("loggedInUser", loggedInUser);

            // ADD THIS: Get tasks for this project
            List<Task> tasks = taskService.getTasksByProjectId(projectId);
            model.addAttribute("tasks", tasks);

            // ADD THIS: Get subprojects for this project
            List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(projectId);
            model.addAttribute("subProjects", subProjects);

            return "project-details";
        }

        return "redirect:/my-projects?error=Ingen+rettigheder";
    }
    @GetMapping("/projects/{id}/edit")
    public String showEditProjectForm(@PathVariable("id") Long projectId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        User loggedInUser = loadUser(userDetails);
        if(loggedInUser==null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if(projectOpt.isEmpty()){
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();
        if(!projectService.userCanEditProject(loggedInUser, project)) {
            return "redirect:/my-projects?error=Ikke+tilladelser+til+at+redigere";
        }

        model.addAttribute("project", project);
        model.addAttribute("loggedInUser", loggedInUser);
        return "edit-project";
    }

    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable("id") Long projectId,
                                @RequestParam("name") String name,
                                @RequestParam("description") String description,
                                @RequestParam("startDate") String startDateStr,
                                @RequestParam("endDate") String endDateStr,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        User loggedInUser = loadUser(userDetails);
        if(loggedInUser==null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if(projectOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("error", "Projekt ikke fundet");
            return "redirect:/my-projects";
        }

        Project project = projectOpt.get();
        if(!projectService.userCanEditProject(loggedInUser, project)) {
            redirectAttributes.addFlashAttribute("error",
                    "Ingen rettigheder til at redigere projektet");
            return "redirect:/projects/" + projectId;
        }

        String errorMessage = projectService.updateProjectDetails(project, name, description,
                startDateStr, endDateStr);
        if(errorMessage != null){
            model.addAttribute("project", project);
            model.addAttribute("error" , errorMessage);
            model.addAttribute("loggedInUser", loggedInUser);
            return "edit-project";
        }

        redirectAttributes.addFlashAttribute("success", "Projekt opdateret!");
        return "redirect:/projects/" + projectId;
    }

    // Intern hjælper til at hente brugeren baseret på Spring Security login
    private User loadUser(UserDetails userDetails) {
        if(userDetails==null) return null;
        return userService.getUserByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/projects/create")
    public String showCreateProjectForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        Project newProject = new Project(); // tomt projekt til formular
        newProject.setCreatedBy(loggedInUser); // evt. forudfyld "ejer"
        model.addAttribute("project", newProject);
        model.addAttribute("loggedInUser", loggedInUser);
        return "create-project"; // <-- lav en HTML-side med formular her
    }

    @PostMapping("/projects/create")
    public String createProject(@ModelAttribute Project project,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        project.setCreatedBy(loggedInUser);

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
    public String showDeleteConfirmation(@PathVariable("id") Long projectId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         Model model) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();

        // Check if user has delete permission
        if (!projectService.userCanDeleteProject(loggedInUser, project)) {
            return "redirect:/my-projects?error=Ingen+tilladelse+til+at+slette+projektet";
        }

        model.addAttribute("project", project);
        model.addAttribute("loggedInUser", loggedInUser);
        return "delete-project-confirmation";
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable("id") Long projectId,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) return "redirect:/login";

        if (!verifyUserPassword(userDetails.getUsername(), confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Forkert adgangskode. Kunne ikke slette projekt.");
            return "redirect:/projects/" + projectId + "/edit";
        }

        boolean deleted = projectService.deleteProject(projectId, loggedInUser);

        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Projekt er blevet slettet");
        } else {
            redirectAttributes.addFlashAttribute("error", "Kunne ikke slette projekt");
        }

        return "redirect:/my-projects";
    }
    @GetMapping("/projects/{projectId}/subprojects/create")
    public String showCreateSubprojectForm(@PathVariable long projectId,
                                           @AuthenticationPrincipal UserDetails userDetails,
                                           Model model) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return "redirect:/my-projects?error=Projekt+ikke+fundet";
        }

        SubProject subProject = new SubProject();
        subProject.setProjectId(projectId);
        model.addAttribute("subProject", subProject);
        model.addAttribute("project", projectOpt.get());
        model.addAttribute("loggedInUser", loggedInUser);

        return "create-subproject";
    }

    // Create a new subproject
    @PostMapping("/projects/{projectId}/subprojects/create")
    public String createSubproject(@PathVariable long projectId,
                                   @ModelAttribute SubProject subProject,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Debug logging
        System.out.println("Creating subproject: " + subProject.getName());
        System.out.println("Project ID: " + projectId);
        System.out.println("SubProject object: " + subProject);

        try {
            subProject.setProjectId(projectId);
            subProject.setCreatedAt(LocalDateTime.now());

            SubProject created = subProjectService.createSubProject(subProject);
            System.out.println("Created subproject with ID: " + (created != null ? created.getSubProjectId() : "null"));

            redirectAttributes.addFlashAttribute("success", "Subprojekt oprettet!");
            return "redirect:/projects/" + projectId;
        } catch (Exception e) {
            System.err.println("Error creating subproject: " + e.getMessage());
            e.printStackTrace(); // This will help us see the full error

            model.addAttribute("error", e.getMessage());
            model.addAttribute("subProject", subProject);

            // Get project for display
            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            if (projectOpt.isPresent()) {
                model.addAttribute("project", projectOpt.get());
            }
            model.addAttribute("loggedInUser", loggedInUser);

            return "create-subproject";
        }
    }
}
