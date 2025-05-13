package com.example.alphasolutionsv2.controller;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.alphasolutionsv2.model.Project;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
}
