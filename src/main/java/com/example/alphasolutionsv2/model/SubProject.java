package com.example.alphasolutionsv2.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubProject {
    private long subProjectId;
    private long projectId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private Project project;
    // Default constructor
    public SubProject() {}

    // Constructor with all fields
    public SubProject(long subProjectId, long projectId, String name, String description,
                      LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        this.subProjectId = subProjectId;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }

    // Getters og setters
    public long getSubProjectId() {
        return subProjectId;
    }

    public void setSubProjectId(long subProjectId) {
        this.subProjectId = subProjectId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            this.projectId = project.getProjectId();
        }
    }
}
