package com.example.alphasolutionsv2.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private long taskId;
    private Long subProjectId;    // Renamed to match database schema
    private String name;
    private String description;
    private Long assignedTo;      // Changed to Long to match database schema
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    // Object references (not directly in database)
    private SubProject subProject;
    private User assignedUser;

    public Task() {
        // Default constructor
    }

    // Constructor with all database fields
    public Task(long taskId, Long subProjectId, String name, String description,
                Long assignedTo, String status, LocalDate dueDate, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.subProjectId = subProjectId;
        this.name = name;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    // Constructor with both database fields and object references
    public Task(long taskId, Long subProjectId, String name, String description,
                Long assignedTo, String status, LocalDate dueDate, LocalDateTime createdAt,
                SubProject subProject, User assignedUser) {
        this.taskId = taskId;
        this.subProjectId = subProjectId;
        this.name = name;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.subProject = subProject;
        this.assignedUser = assignedUser;
    }

    // Getters and Setters
    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public Long getSubProjectId() {
        return subProjectId;
    }

    public void setSubProjectId(Long subProjectId) {
        this.subProjectId = subProjectId;
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

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public SubProject getSubProject() {
        return subProject;
    }

    public void setSubProject(SubProject subProject) {
        this.subProject = subProject;
        if (subProject != null) {
            this.subProjectId = subProject.getSubProjectId();
        }
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
        if (assignedUser != null) {
            this.assignedTo = assignedUser.getUserId();
        }
    }

    // Helper method to get the related project ID via subProject
    public Long getProjectId() {
        if (subProject != null && subProject.getProject() != null) {
            return subProject.getProject().getProjectId();
        }
        return null;
    }
}