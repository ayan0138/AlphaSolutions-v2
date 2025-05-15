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

    // NEGATIVE TESTS

    @Test
    void testShouldThrow_whenNameIsNull() {
        Project p = validProject();
        p.setName(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(p)
        );
        assertEquals("Projekt navn er påkrævet", ex.getMessage());
    }

    @Test
    void testShouldThrow_whenStartDateIsNull() {
        Project p = validProject();
        p.setStartDate(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(p)
        );
        assertEquals("Startdato er påkrævet", ex.getMessage());
    }

    @Test
    void testShouldThrow_whenEndDateIsNull() {
        Project p = validProject();
        p.setEndDate(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(p)
        );
        assertEquals("Slutdato er påkrævet", ex.getMessage());
    }

    @Test
    void testShouldThrow_whenUserMissing() {
        Project p = validProject();
        p.setCreatedBy(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(p)
        );
        assertEquals("Projektet skal tilknyttes en gyldig bruger", ex.getMessage());
    }

    @Test
    void testShouldThrow_whenUserIdInvalid() {
        Project p = validProject();
        User u = new User();
        u.setUserId(0L);
        p.setCreatedBy(u);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                projectService.createProject(p)
        );
        assertEquals("Projektet skal tilknyttes en gyldig bruger", ex.getMessage());
    }

    // POSITIVE TESTS

    @Test
    void testShouldSetCreatedAt_whenMissing() {
        Project p = validProject();
        p.setCreatedAt(null);

        projectService.createProject(p);

        assertNotNull(p.getCreatedAt());
        verify(projectRepository).save(p);
    }

    @Test
    void testShouldSaveProject_whenValid() {
        Project p = validProject();

        projectService.createProject(p);

        verify(projectRepository).save(p);
    }

    // UTIL

    private Project validProject() {
        Project p = new Project();
        p.setName("Projekt A");
        p.setStartDate(LocalDate.now());
        p.setEndDate(LocalDate.now().plusDays(5));
        User u = new User();
        u.setUserId(1L);
        p.setCreatedBy(u);
        return p;
    }
}