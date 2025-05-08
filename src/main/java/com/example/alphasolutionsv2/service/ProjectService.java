package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void createProject(Project project) {
        // validation checks
        if (project.getName() == null || project.getName().isEmpty()) {
            throw new IllegalArgumentException("Projekt navn er påkrævet");
        }

        if (project.getStartDate() == null) {
            throw new IllegalArgumentException("Startdato er påkrævet");
        }

        if (project.getEndDate() == null) {
            throw new IllegalArgumentException("Slutdato er påkrævet");
        }

        if (project.getCreatedBy() == null) {
            throw new IllegalArgumentException("Projektet skal tilknyttes en bruger - feltet 'createdBy' mangler");
        }

        Long userId = project.getCreatedBy().getUserId();
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Brugerens ID er ugyldigt - oprettelse af projekt afvist");
        }

        if (project.getCreatedat() == null) {
            project.setCreatedat(LocalDateTime.now());
        }

        projectRepository.save(project);
    }

    public List<Project> getProjectsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Ugyldig bruger-ID er ugyldig");
        }

        return projectRepository.findUserById(userId);
    }

    public Optional<Project> getProjectById(Long projectId) {
        return projectRepository.findById(projectId);
    }

    public boolean userCanEditProject(Long userId, Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return false;
        }

        Project project = projectOptional.get();
        if (project.getUserId() != null && project.getUserId().equals(userId)) {
            return true;
        }

        return projectRepository.userHasRoleInProject(userId, projectId);
    }

    public boolean userCanViewProject(Long userId, Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return false;
        }

        Project project = projectOptional.get();
        if (project.getUserId() != null && project.getUserId().equals(userId)) {
            return true;
        }

        return projectRepository.userHasAccessToProject(userId, projectId);
    }

    // Update the project
    public boolean updateProject(Project project) {
        try {
            if (project.getName() == null || project.getName().isEmpty()) {
                return false;
            }

            if (project.getStartDate() == null || project.getEndDate() == null) {
                return false;
            }

            Optional<Project> existingProject = projectRepository.findById(project.getProjectId());
            if (existingProject.isEmpty()) {
                return false;
            }

            projectRepository.updateProject(project);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
