package service;

import model.Project;
import org.springframework.stereotype.Service;
import repository.ProjectRepository;

@Service
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
}
