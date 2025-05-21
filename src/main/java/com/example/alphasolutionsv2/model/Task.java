package com.example.alphasolutionsv2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private long taskId;
    private Long subProjectId;
    private String name;
    private String description;
    private Long assignedToId;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private BigDecimal price;
    private Double estimatedHours;
    private Double hourlyRate;
    private Long projectId; // This is for form binding, not stored in database
    private SubProject subProject;
    private User assignedUser;

    public Task() {
        // Default constructor
    }

    // Constructor with all database fields
    public Task(long taskId, Long subProjectId, String name, String description,
                Long assignedTo, String status, LocalDate dueDate, LocalDateTime createdAt, BigDecimal price,
                Double estimatedHours, Double hourlyRate,User assignedUser) {
        this.taskId = taskId;
        this.subProjectId = subProjectId;
        this.name = name;
        this.description = description;
        this.assignedToId = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.price = price;
        this.estimatedHours = estimatedHours;
        this.hourlyRate = hourlyRate;
        this.assignedUser = assignedUser;
    }

    // Alle getters og setters
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

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
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
            this.assignedToId = assignedUser.getUserId();
        }
    }
    public Long getAssignedTo() {
        return assignedToId;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedToId = assignedTo;
    }
}

