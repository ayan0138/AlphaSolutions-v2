package com.example.alphasolutionsv2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubProject {
    private long subProjectId;
    private Project project;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private BigDecimal price;

    public SubProject() {
        // Default constructor
    }

    public SubProject(long subProjectId, Project project, String name, String description, LocalDateTime startDate,
                      LocalDateTime endDate, LocalDateTime createdAt,  BigDecimal price) {
        this.subProjectId = subProjectId;
        this.project = project;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.price = price;
    }

    public long getSubProjectId() {
        return subProjectId;
    }

    public void setSubProjectId(long subProjectId) {
        this.subProjectId = subProjectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
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
}
