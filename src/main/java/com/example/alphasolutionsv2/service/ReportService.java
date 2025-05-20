package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.ProjectReport;
import com.example.alphasolutionsv2.model.ReportSummary;
import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ProjectService projectService;
    private final SubProjectService subProjectService;
    private final TaskService taskService;

    public ReportService(ProjectService projectService,
                         SubProjectService subProjectService,
                         TaskService taskService) {
        this.projectService = projectService;
        this.subProjectService = subProjectService;
        this.taskService = taskService;
    }

    /**
     * Generates a complete project report with calculations
     */
    public ProjectReport generateProjectReport(Long projectId, User generatedBy) {
        ProjectReportData data = getProjectReportData(projectId);

        // Beregn summary
        ReportSummary summary = calculateReportSummary(data.getTasks());

        // Gruppér tasks efter subprojekter for bedre organisering
        Map<String, List<Task>> tasksBySubProject = groupTasksBySubProject(data.getTasks());

        return new ProjectReport(
                data.getProject(),
                data.getSubProjects(),
                data.getTasks(),
                tasksBySubProject,
                summary,
                LocalDateTime.now(),
                generatedBy
        );
    }

    /**
     * Retrieves all project data including subprojects and tasks
     * for report generation
     */
    public ProjectReportData getProjectReportData(Long projectId) {
        // Get hovedeprojektet
        Optional<Project> projectOpt = projectService.getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            throw new IllegalArgumentException("Projekt ikke fundet med ID: " + projectId);
        }

        Project project = projectOpt.get();

        // Get alle subprojekter for projektet
        List<SubProject> subProjects = subProjectService.getSubProjectsByProjectId(projectId);

        // Get alle tasks for projektet
        List<Task> tasks = taskService.getTasksByProjectId(projectId);

        // Enrich tasks med subprojekt informationer
        for (Task task : tasks) {
            if (task.getSubProjectId() != null) {
                SubProject subProject = subProjectService.getSubProjectById(task.getSubProjectId());
                task.setSubProject(subProject);
            }
        }

        return new ProjectReportData(project, subProjects, tasks);
    }

    /**
     * Calculates summary statistics for the report
     */
    private ReportSummary calculateReportSummary(List<Task> tasks) {
        double totalEstimatedHours = tasks.stream()
                .filter(task -> task.getEstimatedHours() != null)
                .mapToDouble(Task::getEstimatedHours)
                .sum();

        double totalCost = tasks.stream()
                .filter(task -> task.getEstimatedHours() != null && task.getHourlyRate() != null)
                .mapToDouble(task -> task.getEstimatedHours() * task.getHourlyRate())
                .sum();

        int totalTasks = tasks.size();

        // Gruppér tasks efter status
        Map<String, Long> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatus() != null ? task.getStatus() : "UNKNOWN",
                        Collectors.counting()
                ));

        // Konverter Long til Integer for nemmere håndtering
        Map<String, Integer> tasksByStatusInt = tasksByStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().intValue()
                ));

        return new ReportSummary(totalEstimatedHours, totalCost, totalTasks, tasksByStatusInt);
    }

    /**
     * Groups tasks by their subproject names for better report organization
     */
    private Map<String, List<Task>> groupTasksBySubProject(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(task ->
                        task.getSubProject() != null ? task.getSubProject().getName() : "Ingen subprojekt"
                ));
    }

    /**
     * Data structure to hold all project report data
     */
    public static class ProjectReportData {
        private final Project project;
        private final List<SubProject> subProjects;
        private final List<Task> tasks;

        public ProjectReportData(Project project, List<SubProject> subProjects, List<Task> tasks) {
            this.project = project;
            this.subProjects = subProjects;
            this.tasks = tasks;
        }

        public Project getProject() {
            return project;
        }

        public List<SubProject> getSubProjects() {
            return subProjects;
        }

        public List<Task> getTasks() {
            return tasks;
        }
    }
}