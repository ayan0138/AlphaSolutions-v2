package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.User;
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

    // =======================
    //  NEGATIVE TESTS (fejlhåndtering)
    // =======================

    @Test
    void testCreateProject_shouldThrowException_whenNameIsNull() {
        User user = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName(null); // null navn
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(7));
        project.setCreatedBy(user);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(project);
        });

        assertEquals("Projekt navn er påkrævet", exception.getMessage());
    }


    @Test
    void testCreateProject_shouldThrowException_whenStartDateMissing() {
        User user = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("Test");
        project.setStartDate(null); // mangler
        project.setEndDate(LocalDate.now());
        project.setCreatedBy(user);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(project);
        });

        assertEquals("Startdato er påkrævet", exception.getMessage());
    }

    @Test
    void testCreateProject_shouldThrowException_whenEndDateMissing() {
        User user = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("Test");
        project.setStartDate(LocalDate.now());
        project.setEndDate(null); // mangler
        project.setCreatedBy(user);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(project);
        });

        assertEquals("Slutdato er påkrævet", exception.getMessage());
    }

    // ========================
    //  POSITIVE TESTS (OK gyldige scenarier)
    // ========================

    @Test
    void testCreateProject_shouldThrowException_whenUserIdInvalid() {
        User user = new User();
        user.setUserId(0L); // ugyldigt ID

        Project project = new Project();
        project.setName("Test");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(7));
        project.setCreatedBy(user);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(project);
        });

        assertEquals("Brugerens ID er ugyldigt - oprettelse af projekt afvist", exception.getMessage());
    }

    @Test
    void testShouldSetCreatedAt_whenMissing() {
        User user = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("Test");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(7));
        project.setCreatedBy(user);
        project.setCreatedAt(null); // simuler at dato mangler

        projectService.createProject(project);

        assertNotNull(project.getCreatedAt());
        verify(projectRepository).save(project);
    }

    @Test
    void testShouldSaveProject_whenAllFieldsValid() {
        User user = new User();
        user.setUserId(1L);

        Project project = new Project();
        project.setName("Valid Name");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(5));
        project.setCreatedBy(user);

        projectService.createProject(project);

        verify(projectRepository).save(project);
    }






}