package service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.alphasolutionsv2.repository.ProjectRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {
    private ProjectRepository projectRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectService = new ProjectService(projectRepository);
    }

    @Test
    void testCreateProject_shouldCallSave_whenProjectIsValid() {
        // Arrange
        User user  = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("Testprojekt");
        project.setDescription("Testbeskrivelse");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(7));
        project.setCreatedBy(user);
        project.setCreatedAt(LocalDateTime.now());

        // Act.
        projectService.createProject(project);

        // Assert.
        verify(projectRepository,times(1)).save(project);
        assertNotNull(project.getCreatedAt(), "createdAt skal automatisk sættes");
    }

    @Test
    void testCreateProject_shouldThrowException_whenNameIsMissing() {
        // Arrange
        User user  = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(7));
        project.setCreatedBy(user);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           projectService.createProject(project);
        });

        assertEquals("Projekt navn er påkrævet", exception.getMessage());

    }
}