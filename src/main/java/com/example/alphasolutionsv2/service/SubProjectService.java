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
        // Tilføj validering
        if (subProject.getName() == null || subProject.getName().isEmpty()) {
            throw new IllegalArgumentException("Subprojekt navn er påkrævet");
        }

        if (subProject.getProjectId() == null || subProject.getProjectId() == 0) {
            throw new IllegalArgumentException("Projekt ID er påkrævet");
        }

        // Set oprettelsestidpunkt  hvis det ikke allered er set
        if (subProject.getCreatedAt() == null) {
            subProject.setCreatedAt(LocalDateTime.now());
        }

        return subProjectRepository.save(subProject);
    }

    /**
     * Update an existing subproject
     */
    public SubProject updateSubProject(SubProject subProject) {
        return subProjectRepository.save(subProject);
    }

    /**
     * Delete a subproject
     */
    public void deleteSubProject(Long subProjectId) {
        subProjectRepository.deleteById(subProjectId);
    }

    /**
     * Check if a subproject exists with given ID and belongs to given project
     */
    public boolean existsByIdAndProjectId(Long subProjectId, Long projectId) {
        return subProjectRepository.existsByIdAndProjectId(subProjectId, projectId);
    }
    public Long getProjectIdBySubProjectId(Long subProjectId) {
        SubProject subProject = getSubProjectById(subProjectId);
        return subProject != null ? subProject.getProjectId() : null;
    }
}