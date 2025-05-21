package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import com.example.alphasolutionsv2.service.SubProjectService;
import com.example.alphasolutionsv2.service.TaskService;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    private final TaskService taskService;
    private final ProjectService projectService;
    private final SubProjectService subProjectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService,
                          SubProjectService subProjectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.subProjectService = subProjectService;
        this.userService = userService;
    }

    // ============= HELPER METHODS =============

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

        // Add projects if projectId is not set
        if (task.getProjectId() == null) {
            List<Project> projects = projectService.getProjectsByUserId(loggedInUser.getUserId());
            model.addAttribute("projects", projects);
        } else {
            // Add subprojects for the specific project
            List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(task.getProjectId());
            model.addAttribute("subProjects", subProjects);

            // Add project details
            Optional<Project> projectOpt = projectService.getProjectById(task.getProjectId());
            projectOpt.ifPresent(project -> model.addAttribute("project", project));

            // Add selected subproject details if available
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

    // ============= VIEW METHODS =============

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

    // ============= CREATE METHODS =============

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
            // Set default values
            task.setCreatedAt(LocalDateTime.now());
            if (task.getStatus() == null || task.getStatus().isEmpty()) {
                task.setStatus("PENDING");
            }

            taskService.createTask(task);

            // Redirect based on context
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

    // ============= EDIT METHODS =============

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

        // Ensure project ID is set
        Long projectId = task.getProjectId();
        if (projectId == null && task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            task.setProjectId(projectId);
        }

        // Load current subproject info
        if (task.getSubProjectId() != null) {
            SubProject currentSubProject = subProjectService.getSubProjectById(task.getSubProjectId());
            task.setSubProject(currentSubProject);
        }

        model.addAttribute("task", task);
        model.addAttribute("loggedInUser", loggedInUser);

        // Add subprojects and project info
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

            // Redirect based on context
            if (task.getProjectId() != null) {
                return "redirect:/tasks/project/" + task.getProjectId();
            } else {
                return "redirect:/tasks";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("task", task);
            model.addAttribute("loggedInUser", loggedInUser);

            // Re-populate form data
            if (task.getProjectId() != null) {
                List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(task.getProjectId());
                model.addAttribute("subProjects", subProjects);
            }

            return "edit-task";
        }
    }

    // ============= DELETE METHODS =============

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

        // Ensure project ID is set
        Long projectId = task.getProjectId();
        if (projectId == null && task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            task.setProjectId(projectId);
        }

        // Load subproject info
        if (task.getSubProjectId() != null) {
            SubProject subProject = subProjectService.getSubProjectById(task.getSubProjectId());
            task.setSubProject(subProject);
        }

        // Add project for display
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

        // Verify password
        if (!verifyPassword(userDetails.getUsername(), confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Forkert adgangskode. Kunne ikke slette opgave.");
            return "redirect:/tasks/" + taskId + "/delete" + (from != null ? "?from=" + from : "");
        }

        try {
            // Get IDs before deletion for redirect
            Long projectId = task.getProjectId();
            Long subProjectId = task.getSubProjectId();

            if (projectId == null && subProjectId != null) {
                projectId = subProjectService.getProjectIdBySubProjectId(subProjectId);
            }

            taskService.deleteTask(taskId);
            redirectAttributes.addFlashAttribute("success", "Opgave er blevet slettet");

            // Smart redirect based on context
            if ("subproject".equals(from) && subProjectId != null) {
                return "redirect:/tasks/subproject/" + subProjectId;
            } else if (projectId != null) {
                return "redirect:/projects/" + projectId;
            } else {
                return "redirect:/my-projects";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kunne ikke slette opgave: " + e.getMessage());
            return "redirect:/tasks/" + taskId + "/delete" + (from != null ? "?from=" + from : "");
        }
    }
    // Tilføj disse metoder til din eksisterende TaskController

// ============= ASSIGN TASK METHODS =============

    @GetMapping("/{taskId}/assign")
    public String showAssignTaskForm(@PathVariable long taskId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        // Tjek om brugeren er admin eller projektleder
        if (!"Admin".equals(loggedInUser.getRole().getRoleName()) &&
                !"Projektleder".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/my-projects?error=Du har ikke rettigheder til at tildele opgaver";
        }

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "redirect:/my-projects?error=Opgave+ikke+fundet";
        }

        // Hent alle medarbejdere (brugere med rollen "Medarbejder")
        List<User> employees = userService.getAllEmployees();

        // Find projektet
        Long projectId = null;
        // Hvis opgaven har et direkte projekt-ID
        if (task.getProjectId() != null) {
            projectId = task.getProjectId();
        }
        // Hvis opgaven har et subprojekt-ID, find projektet gennem subprojektet
        else if (task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
        }

        // Kun forsøg at hente projektet, hvis vi har et projektId og det er større end 0
        if (projectId != null && projectId > 0) {
            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            projectOpt.ifPresent(project -> model.addAttribute("project", project));
        }

        model.addAttribute("task", task);
        model.addAttribute("employees", employees);
        model.addAttribute("loggedInUser", loggedInUser);

        return "assign-task";
    }

    @PostMapping("/{taskId}/assign")
    public String assignTask(@PathVariable long taskId,
                             @RequestParam(required = false) Long employeeId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        // Tjek om brugeren er admin eller projektleder
        if (!"Admin".equals(loggedInUser.getRole().getRoleName()) &&
                !"Projektleder".equals(loggedInUser.getRole().getRoleName())) {
            redirectAttributes.addAttribute("error", "Du har ikke rettigheder til at tildele opgaver");
            return "redirect:/my-projects";
        }

        try {
            // Hent opgaven
            Task task = taskService.getTaskById(taskId);
            if (task == null) {
                redirectAttributes.addFlashAttribute("error", "Opgave ikke fundet");
                return "redirect:/my-projects";
            }

            // Debugging: Vis opgavedetaljer før opdatering
            System.out.println("Task before assignment: ID=" + task.getTaskId() +
                    ", assignedToId=" + task.getAssignedToId() +
                    ", assignedUser=" + (task.getAssignedUser() != null ?
                    task.getAssignedUser().getUsername() : "null"));

            if (employeeId == null) {
                // Fjern tildeling
                System.out.println("Removing assignment from task: " + task.getTaskId());
                task.setAssignedTo(null);  // Brug setAssignedTo, da det er den metode, der findes i Task-klassen
                task.setAssignedUser(null);
                taskService.updateTask(task);
                redirectAttributes.addFlashAttribute("success", "Opgavetildeling er blevet fjernet");
            } else {
                // Tildel opgaven til den valgte medarbejder
                User employee = userService.getUserById(employeeId).orElse(null);
                if (employee == null) {
                    redirectAttributes.addFlashAttribute("error", "Medarbejder ikke fundet");
                    return "redirect:/tasks/" + taskId + "/assign";
                }

                System.out.println("Assigning employee: " + employee.getUsername() +
                        " (ID: " + employee.getUserId() + ") to task: " + task.getTaskId());

                task.setAssignedTo(employeeId);  // Brug setAssignedTo, da det er den metode, der findes i Task-klassen
                task.setAssignedUser(employee);
                taskService.updateTask(task);
                redirectAttributes.addFlashAttribute("success", "Opgaven er blevet tildelt til " + employee.getUsername());
            }

            // Find projekt-ID for redirect
            Long projectId = null;

            // Forsøg først direkte projektID
            if (task.getProjectId() != null && task.getProjectId() > 0) {
                projectId = task.getProjectId();
            }
            // Dernæst via subprojekt
            else if (task.getSubProjectId() != null) {
                projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            }

            // Redirect til projektet eller projektoversigten
            if (projectId != null && projectId > 0) {
                Optional<Project> projectOpt = projectService.getProjectById(projectId);
                if (projectOpt.isPresent()) {
                    return "redirect:/projects/" + projectId;
                }
            }

            // Fallback til projektoversigt
            return "redirect:/my-projects";

        } catch (Exception e) {
            e.printStackTrace(); // Tilføj stacktrace til konsolloggen for at se alle detaljer
            redirectAttributes.addFlashAttribute("error", "Kunne ikke tildele opgave: " + e.getMessage());
            return "redirect:/tasks/" + taskId + "/assign";
        }
    }
    // Add this method to TaskController class

    @GetMapping("/my-assigned-tasks")
    public String showMyAssignedTasks(@AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        // Get all tasks assigned to the logged-in user
        List<Task> assignedTasks = taskService.getTasksByAssignedUserId(loggedInUser.getUserId());

        model.addAttribute("tasks", assignedTasks);
        model.addAttribute("loggedInUser", loggedInUser);

        return "my-assigned-tasks";
    }
    // Add this to TaskController class
    @GetMapping("/my-tasks")
    public String showMyTasks(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        // Get all tasks assigned to the logged-in user
        List<Task> assignedTasks = taskService.getTasksByAssignedUserId(loggedInUser.getUserId());

        model.addAttribute("tasks", assignedTasks);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("title", "Mine Opgaver");

        return "my-tasks";
    }
    // Add this method to TaskController class
    @GetMapping("/view/{taskId}")
    public String viewTask(@PathVariable long taskId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User loggedInUser = loadUser(userDetails);
        String redirect = redirectIfNotLoggedIn(loggedInUser);
        if (redirect != null) return redirect;

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return "redirect:/tasks/my-tasks?error=Opgave+ikke+fundet";
        }

        // Check if the user is assigned to this task or has admin/projektleder role
        boolean isAssigned = task.getAssignedToId() != null &&
                task.getAssignedToId().equals(loggedInUser.getUserId());
        boolean hasHigherRole = "Admin".equals(loggedInUser.getRole().getRoleName()) ||
                "Projektleder".equals(loggedInUser.getRole().getRoleName());

        if (!isAssigned && !hasHigherRole) {
            return "redirect:/tasks/my-tasks?error=Du+har+ikke+adgang+til+denne+opgave";
        }

        // Load project information
        Long projectId = task.getProjectId();
        if (projectId == null && task.getSubProjectId() != null) {
            projectId = subProjectService.getProjectIdBySubProjectId(task.getSubProjectId());
            task.setProjectId(projectId);
        }

        if (projectId != null) {
            Optional<Project> projectOpt = projectService.getProjectById(projectId);
            projectOpt.ifPresent(project -> model.addAttribute("project", project));
        }

        model.addAttribute("task", task);
        model.addAttribute("loggedInUser", loggedInUser);

        return "view-task";
    }
}
