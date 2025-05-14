package com.example.alphasolutionsv2.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Complete project report data structure
 */
public class ProjectReport {
    private final Project project;
    private final List<SubProject> subProjects;
    private final List<Task> tasks;
    private final Map<String, List<Task>> tasksBySubProject;
    private final ReportSummary summary;
    private final LocalDateTime generatedAt;
    private final User generatedBy;

    public ProjectReport(Project project, List<SubProject> subProjects, List<Task> tasks,
                         Map<String, List<Task>> tasksBySubProject, ReportSummary summary,
                         LocalDateTime generatedAt, User generatedBy) {
        this.project = project;
        this.subProjects = subProjects;
        this.tasks = tasks;
        this.tasksBySubProject = tasksBySubProject;
        this.summary = summary;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
    }

    // Getters
    public Project getProject() { return project; }
    public List<SubProject> getSubProjects() { return subProjects; }
    public List<Task> getTasks() { return tasks; }
    public Map<String, List<Task>> getTasksBySubProject() { return tasksBySubProject; }
    public ReportSummary getSummary() { return summary; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public User getGeneratedBy() { return generatedBy; }
}