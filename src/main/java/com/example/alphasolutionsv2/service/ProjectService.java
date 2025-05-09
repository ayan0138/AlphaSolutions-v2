package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import org.springframework.stereotype.Service;
import com.example.alphasolutionsv2.repository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void createProject(Project project) {

        if(project.getName() == null || project.getName().isEmpty()){
            throw new IllegalArgumentException("Projekt navn er påkrævet");
        }

        if(project.getStartDate() == null){
            throw new IllegalArgumentException("Startdato er påkrævet");
        }

        if(project.getEndDate() == null){
            throw new IllegalArgumentException("Slutdato er påkrævet");
        }

        if(project.getCreatedBy() == null) {
            throw new IllegalArgumentException("Projektet skal tilknyttes en bruger - feltet 'createdBy' mangler");
        }

        long userId = project.getCreatedBy().getUserId();
        if( userId <= 0) {
            throw new IllegalArgumentException("Brugerens ID er ugyldigt - oprettelse af projekt afvist");
        }

        if(project.getCreatedAt() == null){
            project.setCreatedAt(LocalDateTime.now());
        }


        projectRepository.save(project);
    }

    public List<Project> getProjectsByUserId(Long userId) {
        if(userId == null || userId <= 0) {
            throw new IllegalArgumentException("Ugyldig bruger-ID er ugyldig");
        }

        return projectRepository.findUserById(userId);
    }
}
