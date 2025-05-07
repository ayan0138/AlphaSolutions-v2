package model;

import java.time.LocalDateTime;

public class ProjectAssignment {
    private long assignmentId;
    private long userId;
    private long projectId;
    private String projectRole;
    private LocalDateTime created_at;

    public ProjectAssignment(long assignmentId, long userId, long projectId, String projectRole, LocalDateTime created_at) {
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.projectId = projectId;
        this.projectRole = projectRole;
        this.created_at = created_at;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(String projectRole) {
        this.projectRole = projectRole;
    }
    public LocalDateTime getCreated_at(){
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}