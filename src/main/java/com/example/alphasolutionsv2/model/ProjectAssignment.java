package com.example.alphasolutionsv2.model;

import java.time.LocalDateTime;

public class ProjectAssignment {
    private long assignmentId;
    private User user; // FK: userId -> som objekt
    private Project project;  // FK: projectId -> som objekt
    private String projectRole;
    private LocalDateTime createdAt;

    public ProjectAssignment() {
        // Default constructor
    }

    public ProjectAssignment(long assignmentId, User user, Project project, String projectRole, LocalDateTime createdAt) {
        this.assignmentId = assignmentId;
        this.user = user;
        this.project = project;
        this.projectRole = projectRole;
        this.createdAt = createdAt;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(String projectRole) {
        this.projectRole = projectRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}