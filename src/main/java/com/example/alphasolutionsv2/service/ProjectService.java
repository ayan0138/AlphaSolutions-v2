package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.repository.ProjectRepository;
import com.example.alphasolutionsv2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public void createProject(Project project) {
        if (project.getName() == null || project.getName().isEmpty()) {
            throw new IllegalArgumentException("Projekt navn er påkrævet");
        }

        if (project.getStartDate() == null) {
            throw new IllegalArgumentException("Startdato er påkrævet");
        }

        if (project.getEndDate() == null) {
            throw new IllegalArgumentException("Slutdato er påkrævet");
        }

        if (project.getCreatedBy() == null || project.getCreatedBy().getUserId() == null || project.getCreatedBy().getUserId() <= 0) {
            throw new IllegalArgumentException("Projektet skal tilknyttes en gyldig bruger");
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

        // Get all projects the user is directly associated with
        List<Project> projects = projectRepository.findAllProjectsForUser(userId);

        // For employees, also include projects where they have assigned tasks
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent() && "MEDARBEJDER".equalsIgnoreCase(userOpt.get().getRole().getRoleName())) {
            List<Project> projectsWithTasks = projectRepository.findProjectsByAssignedTasks(userId);

            // Combine the lists without duplicates
            for (Project project : projectsWithTasks) {
                if (projects.stream().noneMatch(p -> p.getProjectId().equals(project.getProjectId()))) {
                    projects.add(project);
                }
            }
        }

        return projects;
    }

    public Optional<Project> getProjectById(Long projectId) {
        if (projectId == null || projectId <= 0) {
            // Returnér et tomt Optional i stedet for at kaste en exception
            return Optional.empty();
        }
        return projectRepository.findById(projectId);
    }

    public boolean userCanViewProject(Long userId, Long projectId) {
        // First check if the project exists
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return false;
        }

        Project project = projectOptional.get();

        // Check if user is the creator
        if (project.getCreatedBy() != null && project.getCreatedBy().getUserId() != null &&
                project.getCreatedBy().getUserId().equals(userId)) {
            return true;
        }

        // Check if user has direct project access
        projectRepository.userHasAccessToProject(userId, projectId);

        // For now, temporarily allow all authenticated users to view projects
        return true;
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
            // Tjek for tomt eller manglende projektnavn
            if(name == null || name.isEmpty()) {
                return "Projektnavn er påkrævet";
            }

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

            return null; // Ingen fejl
        } catch (Exception e) {
            return "Ugyldige data: " + e.getMessage();
        }
    }

    public boolean deleteProject(Long projectId, User user) {
        if (projectId == null || projectId <= 0) {
            return false;
        }

        Optional<Project> projectOpt = getProjectById(projectId);
        if (projectOpt.isEmpty()) {
            return false;
        }

        Project project = projectOpt.get();

        // Check if user has permission to delete this project
        if (!userCanDeleteProject(user, project)) {
            return false;
        }

        try {
            projectRepository.deleteById(projectId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean userCanDeleteProject(User user, Project project) {
        if (user == null || project == null || user.getRole() == null) {
            return false;
        }

        // Only admins and the project creator can delete projects
        return "Admin".equalsIgnoreCase(user.getRole().getRoleName()) ||
                user.getUserId().equals(project.getCreatedBy().getUserId());
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}