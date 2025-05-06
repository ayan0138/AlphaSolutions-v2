package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private long taskId;
    private SubProject subProject; // FK som objekt
    private String name;
    private String description;
    private User assignedTo; // FK som objekt
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    public Task() {
        // Default constructor
    }

    public Task(long taskId, SubProject subProject, String name, String description, User assignedTo, String status,
                LocalDate dueDate, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.subProject = subProject;
        this.name = name;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public SubProject getSubProject() {
        return subProject;
    }

    public void setSubProject(SubProject subProject) {
        this.subProject = subProject;
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

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
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
}
