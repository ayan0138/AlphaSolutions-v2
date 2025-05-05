package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {
    private long projectId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private long createdBy; // FK til User
    private LocalDateTime created_at;

    public Project() {
        // Default constructor
    }

    public Project(long projectId, String name, String description, LocalDate startDate,
                   LocalDate endDate, long createdBy, LocalDateTime created_at) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.created_at = created_at;
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

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }
    public LocalDateTime getCreated_at(){
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
