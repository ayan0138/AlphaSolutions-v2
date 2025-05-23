package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubProjectService {

    private final SubProjectRepository subProjectRepository;

    public SubProjectService(SubProjectRepository subProjectRepository) {
        this.subProjectRepository = subProjectRepository;
    }

    /**
     * Get all subprojects for a specific project
     */
    public List<SubProject> getSubProjectsByProjectId(long projectId) {
        return subProjectRepository.findByProjectId(projectId);
    }

    /**
     * Get a specific subproject by its ID
     */
    public SubProject getSubProjectById(long subProjectId) {
        return subProjectRepository.findById(subProjectId);
    }

    /**
     * Create a new subproject
     */
    public SubProject createSubProject(SubProject subProject) {
        // Existing validation
        if (subProject.getName() == null || subProject.getName().isEmpty()) {
            throw new IllegalArgumentException("Subprojekt navn er påkrævet");
        }

        if (subProject.getProjectId() == 0) {
            throw new IllegalArgumentException("Projekt ID er påkrævet");
        }

        // Add date validation
        if (subProject.getStartDate() != null && subProject.getEndDate() != null &&
                subProject.getEndDate().isBefore(subProject.getStartDate())) {
            throw new IllegalArgumentException("Slutdato må ikke være før startdato");
        }

        // Set creation time if not already set
        if (subProject.getCreatedAt() == null) {
            subProject.setCreatedAt(LocalDateTime.now());
        }

        return subProjectRepository.save(subProject);
    }

    /**
     * Update an existing subproject
     */
    public SubProject updateSubProject(SubProject subProject) {
        // Add validation for updates too
        if (subProject.getName() == null || subProject.getName().isEmpty()) {
            throw new IllegalArgumentException("Subprojekt navn er påkrævet");
        }

        // Add date validation
        if (subProject.getStartDate() != null && subProject.getEndDate() != null &&
                subProject.getEndDate().isBefore(subProject.getStartDate())) {
            throw new IllegalArgumentException("Slutdato må ikke være før startdato");
        }

        return subProjectRepository.save(subProject);
    }

    /**
     * Delete a subproject
     */
    public void deleteSubProject(long subProjectId) {
        subProjectRepository.deleteById(subProjectId);
    }

    /**
     * Check if a subproject exists with given ID and belongs to given project
     */
    public boolean existsByIdAndProjectId(long subProjectId, long projectId) {
        return subProjectRepository.existsByIdAndProjectId(subProjectId, projectId);
    }
    public Long getProjectIdBySubProjectId(long subProjectId) {
        SubProject subProject = getSubProjectById(subProjectId);
        return subProject != null ? subProject.getProjectId() : null;
    }

}
