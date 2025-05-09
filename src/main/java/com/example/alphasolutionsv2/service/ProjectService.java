package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Create (Opret projekt)
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
        if (userId <= 0) {
            throw new IllegalArgumentException("Brugerens ID er ugyldigt - oprettelse af projekt afvist");
        }

        if (project.getCreatedAt() == null) {
            project.setCreatedAt(LocalDateTime.now());
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
        if( projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Ugyldig projekt-ID");
        }
        return projectRepository.findById(projectId);
    }

    public boolean userCanViewProject(Long userId, Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return false;
        }

        Project project = projectOptional.get();

        return isOwner(userId, project) || projectRepository.userHasAccessToProject(userId, projectId);
    }

    private boolean isOwner(Long userId, Project project) {
        return project.getUserId() != null && project.getUserId().equals(userId);
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

    public boolean userCanEditProject(User user, Project project) {
        if (user == null || project == null || user.getRole() == null) return false;

        return user.getUserId().equals(project.getCreatedBy().getUserId()) ||
                "Admin".equalsIgnoreCase(user.getRole().getRoleName()) ||
                "Projektleder".equalsIgnoreCase(user.getRole().getRoleName());
    }

    public String updateProjectDetails(Project project, String name, String description,
                                       String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            if (endDate.isBefore(startDate)) {
                return "Slutdato må ikke være før startdato";
            }

            project.setName(name);
            project.setDescription(description);
            project.setStartDate(startDate);
            project.setEndDate(endDate);

            boolean updated = updateProject(project);
            if (!updated) {
                return "Kunne ikke opdatere projektet i databasen";
            }

            return null; // ingen fejl
        } catch (Exception e) {
            return "Ugyldige data: " + e.getMessage();
        }
    }
}
