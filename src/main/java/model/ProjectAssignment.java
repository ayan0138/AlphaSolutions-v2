package model;

public class ProjectAssignment {
    private long assignmentId;
    private long userId;
    private long projectId;
    private String projectRole;

    public ProjectAssignment(long assignmentId, long userId, long projectId, String projectRole) {
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.projectId = projectId;
        this.projectRole = projectRole;
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
}