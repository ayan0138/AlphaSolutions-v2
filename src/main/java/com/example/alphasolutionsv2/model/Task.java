package com.example.alphasolutionsv2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private Long taskId;
    private Long subProjectId;
    private String name;
    private String description;
    private Long assignedTo;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private BigDecimal price;
    private Double estimatedHours;
    private Double hourlyRate;

    // Add projectId field for form binding
    private Long projectId; // This is for form binding, not stored in database

    // Object references (not directly in database)
    private SubProject subProject;
    private User assignedUser;

    public Task() {
        // Default constructor - nødvendig for binding i Thymeleaf
    }

    // Constructor with all database fields
    public Task(Long taskId, Long subProjectId, String name, String description,
                Long assignedTo, String status, LocalDate dueDate, LocalDateTime createdAt, BigDecimal price,
                Double estimatedHours, Double hourlyRate) {
        this.taskId = taskId;
        this.subProjectId = subProjectId;
        this.name = name;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.price = price;
        this.estimatedHours = estimatedHours;
        this.hourlyRate = hourlyRate;
    }

    // Alle getters og setters
    public Long getTaskId() {
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
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    // Add getter and setter for projectId (for form binding)
    public Long getProjectId() {
        if (projectId != null) {
            return projectId;
        }
        // Fallback to getting project ID from subproject
        if (subProject != null && subProject.getProject() != null) {
            return subProject.getProject().getProjectId();
        }
        return null;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
}
