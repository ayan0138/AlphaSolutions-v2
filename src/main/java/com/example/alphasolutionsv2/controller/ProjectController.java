package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.TaskService;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ProjectController {


    private final ProjectService projectService;
    private final UserService userService;
    private final TaskService taskService;


    public ProjectController(ProjectService projectService, UserService userService,TaskService taskService) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService;

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
            return "redirect/my-projects?error=Projekt+ikke+fundet";
        }

        Project project = projectOpt.get();
        if(project.getCreatedBy().getUserId().equals(loggedInUser.getUserId()) ||
                "ADMIN".equalsIgnoreCase(loggedInUser.getRole().getRoleName()) ||
                projectService.userCanViewProject(loggedInUser.getUserId(), projectId)) {

            model.addAttribute("project", project);
            model.addAttribute("loggedInUser", loggedInUser);
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
    @GetMapping("/projects/create") // opretter projekt task 2.1
    public String showCreateProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "create-project";
    }
    @PostMapping("/projects/create")
    public String createProject(@ModelAttribute Project project,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        project.setCreatedBy(loggedInUser);

        try {
            projectService.createProject(project);
            redirectAttributes.addFlashAttribute("success", "Projekt oprettet!");

            // ✨ Redirect direkte til task-oprettelsesformular via project
            return "redirect:/projects/" + project.getProjectId() + "/tasks/create";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/projects/create";
        }

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
}
