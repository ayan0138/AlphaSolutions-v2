package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.TaskService;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {




    private final TaskService taskService;
    private final ProjectService projectService;
    private final SubProjectService subProjectService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public TaskController(TaskService taskService, ProjectService projectService,
                          SubProjectService subProjectService, UserService userService,
                          AuthenticationManager authenticationManager) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.subProjectService = subProjectService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // ============= HJÆLPE-METODER =============

    private User loadUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByUsername(userDetails.getUsername()).orElse(null);
    }

    private String redirectIfNotLoggedIn(User user) {
        return user == null ? "redirect:/login" : null;
    }

    private void populateCreateTaskModel(Model model, Task task, User loggedInUser) {
        model.addAttribute("task", task);
        model.addAttribute("loggedInUser", loggedInUser);

        // Tilføj projekter hvis projectId ikke er angivet
        if (task.getProjectId() == null) {
            List<Project> projects = projectService.getProjectsByUserId(loggedInUser.getUserId());
            model.addAttribute("projects", projects);
        } else {
            // Tilføj subprojekter for det specifikke projekt
            List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(task.getProjectId());
            model.addAttribute("subProjects", subProjects);

            // Tilføj projekt detaljer
            Optional<Project> projectOpt = projectService.getProjectById(task.getProjectId());
            projectOpt.ifPresent(project -> model.addAttribute("project", project));

            // Tilføj valgte subprojekter hvis tilgengelige
            if (task.getSubProjectId() != null) {
                SubProject selectedSubProject = subProjectService.getSubProjectById(task.getSubProjectId());
                if (selectedSubProject != null) {
                    model.addAttribute("subProject", selectedSubProject);
                    task.setSubProject(selectedSubProject);
                }
            }
        }
    }

    private boolean verifyPassword(String username, String password) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication result = authenticationManager.authenticate(authRequest);
            return result.isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }

    // ============= VISNINGS-METODER =============

    @GetMapping("")
    public String showAllTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("loggedInUser", loggedInUser);
        return "tasks-list";
    }

    @GetMapping("/project/{projectId}")
    public String getTasksByProject(@PathVariable long projectId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        model.addAttribute("loggedInUser", loggedInUser);
        return "tasks-list";
    }

    @GetMapping("/subproject/{subProjectId}")
    public String getTasksBySubProject(@PathVariable long subProjectId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        SubProject subProject = subProjectService.getSubProjectById(subProjectId);
        if (subProject == null) {
            return "redirect:/my-projects?error=Subprojekt+ikke+fundet";
        }

        List<Task> tasks = taskService.getTasksBySubProjectId(subProjectId);
        Optional<Project> projectOpt = projectService.getProjectById(subProject.getProjectId());

        model.addAttribute("tasks", tasks);
        model.addAttribute("subProject", subProject);
        model.addAttribute("loggedInUser", loggedInUser);
        projectOpt.ifPresent(project -> model.addAttribute("project", project));

        return "subproject-tasks";
    }

    // ============= OPRET-METODER =============

    @GetMapping("/create")
    public String showCreateTaskForm(@RequestParam(required = false) Long projectId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = new Task();
        if (projectId != null) {
            task.setProjectId(projectId);
        }

        populateCreateTaskModel(model, task, loggedInUser);
        return "create-task";
    }

    @GetMapping("/project/{projectId}/create")
    public String showCreateTaskFormForProject(@PathVariable long projectId,
                                               @RequestParam(required = false) Long subProjectId,
                                               @AuthenticationPrincipal UserDetails userDetails,
                                               Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = new Task();
        task.setProjectId(projectId);
        if (subProjectId != null) {
            task.setSubProjectId(subProjectId);
        }

        populateCreateTaskModel(model, task, loggedInUser);
        return "create-task";
    }

    @PostMapping("/create")
    public String createTask(@ModelAttribute Task task,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        try {
            // Angiv standardværdier
            task.setCreatedAt(LocalDateTime.now());
            if (task.getStatus() == null || task.getStatus().isEmpty()) {
                task.setStatus("PENDING");
            }

            taskService.createTask(task);

            // Redirect baseret på kontekst
            if (task.getProjectId() != null) {
                return "redirect:/tasks/project/" + task.getProjectId();
            } else {
                return "redirect:/tasks";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            populateCreateTaskModel(model, task, loggedInUser);
            return "create-task";
        }
    }

    // ============= REDIGER-METODER =============

    @GetMapping("/edit/{taskId}")
    public String showEditTaskForm(@PathVariable long taskId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            model.addAttribute("error", "Opgave ikke fundet");
            return "redirect:/tasks";
        }

        // Sørg for projectId er angivet
        Long projectId = task.getProjectId();
        if (projectId == null && task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            task.setProjectId(projectId);
        }

        // indlæs oplysninger om det aktuelle subprojekt
        if (task.getSubProjectId() != null) {
            SubProject currentSubProject = subProjectService.getSubProjectById(task.getSubProjectId());
            task.setSubProject(currentSubProject);
        }

        model.addAttribute("task", task);
        model.addAttribute("loggedInUser", loggedInUser);

        // tilføj subproject og projetk info
        if (projectId != null) {
            List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(projectId);
            model.addAttribute("subProjects", subProjects);

            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            projectOpt.ifPresent(project -> model.addAttribute("project", project));
        }

        return "edit-task";
    }

    @PostMapping("/edit/{taskId}")
    public String updateTask(@PathVariable long taskId,
                             @ModelAttribute Task task,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        try {
            task.setTaskId(taskId);
            taskService.updateTask(task);

            // Redirect baseret på kontekst
            if (task.getProjectId() != null) {
                return "redirect:/tasks/project/" + task.getProjectId();
            } else {
                return "redirect:/tasks";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("task", task);
            model.addAttribute("loggedInUser", loggedInUser);

            // Genudfyld formularens data
            if (task.getProjectId() != null) {
                List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(task.getProjectId());
                model.addAttribute("subProjects", subProjects);
            }

            return "edit-task";
        }
    }

    // ============= SLETTE-METODER =============

    @GetMapping("/{taskId}/delete")
    public String showDeleteTaskConfirmation(@PathVariable long taskId,
                                             @RequestParam(required = false) String from,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "redirect:/my-projects?error=Opgave+ikke+fundet";
        }

        // Sørger for projectId er angivet
        Long projectId = task.getProjectId();
        if (projectId == null && task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            task.setProjectId(projectId);
        }

        // indlæs subprojekt oplysninger
        if (task.getSubProjectId() != null) {
            SubProject subProject = subProjectService.getSubProjectById(task.getSubProjectId());
            task.setSubProject(subProject);
        }

        // Tilføj projekt til visning
        if (projectId != null) {
            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            projectOpt.ifPresent(project -> model.addAttribute("project", project));
        }

        model.addAttribute("task", task);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("from", from);

        return "delete-task";
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable long taskId,
                             @RequestParam("confirmPassword") String confirmPassword,
                             @RequestParam(required = false) String from,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Opgave ikke fundet");
            return "redirect:/my-projects";
        }

        // Bekræft adgangskode
        if (!verifyPassword(userDetails.getUsername(), confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Forkert adgangskode. Kunne ikke slette opgave.");
            return buildDeleteRedirectUrl(taskId, from);
        }

        try {
            // Hent ID´s før sletning til redirect
            Long projectId = task.getProjectId();
            Long subProjectId = task.getSubProjectId();

            if (projectId == null && subProjectId != null) {
                projectId = subProjectService.getProjectIdBySubProjectId(subProjectId);
            }

            taskService.deleteTask(taskId);
            redirectAttributes.addFlashAttribute("success", "Opgave er blevet slettet");

            // Smart redirect baseret på kontekst
            if ("subproject".equals(from) && subProjectId != null) {
                return "redirect:/tasks/subproject/" + subProjectId;
            } else if (projectId != null) {
                return "redirect:/projects/" + projectId;
            } else {
                return "redirect:/my-projects";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kunne ikke slette opgave: " + e.getMessage());
            return buildDeleteRedirectUrl(taskId, from);
        }
    }

    private String buildDeleteRedirectUrl(long taskId, String from) {
        return "redirect:/tasks/" + taskId + "/delete" + (from != null ? "?from=" + from : "");
    }

}