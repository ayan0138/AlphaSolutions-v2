package service;

import model.Project;
import repository.ProjectRepository;

import java.util.List;

public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void createProject(Project project) {

        if(project.getName() == null || project.getName().isEmpty()){
            throw new IllegalArgumentException("Project name is required");
        }
        projectRepository.save(project);
    }

    public List<Project> getProjectsByUser(long userId) {
        return projectRepository.findByCreatedBy(userId);
    }
}
