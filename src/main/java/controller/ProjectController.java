package controller;

import model.Project;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import service.ProjectService;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Viser formularen til at oprette projekt
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "create-project"; // Matcher HTML-filens navn
    }

    // Modtager og gemmer projektet
    @PostMapping("/create")
    public String showCreateForm(Model model, Project project) {
        projectService.createProject(project);
        return "redirect:/projects/success"; // Eller tilbage til liste/oversigt
    }
}
