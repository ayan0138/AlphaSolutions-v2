package com.example.alphasolutionsv2.service;

import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.repository.SubProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubProjectServiceTest {
    private SubProjectRepository subProjectRepository;
    private SubProjectService subProjectService;

    @BeforeEach
    void setUp() {
        subProjectRepository = mock(SubProjectRepository.class);
        subProjectService = new SubProjectService(subProjectRepository);
    }

    @Test
    void testGetSubProjectsByProjectId_shouldReturnList() {
        when(subProjectRepository.findByProjectId(1L)).thenReturn(List.of(new SubProject()));

        List<SubProject> result = subProjectService.getSubProjectsByProjectId(1L);

        assertEquals(1, result.size());
        verify(subProjectRepository).findByProjectId(1L);
    }

    @Test
    void testGetSubProjectById_shouldReturnSubProject() {
        SubProject subProject = new SubProject();
        when(subProjectRepository.findById(2L)).thenReturn(subProject);

        SubProject result = subProjectService.getSubProjectById(2L);

        assertNotNull(result);
        verify(subProjectRepository).findById(2L);
    }

    @Test
    void testCreateSubProject_shouldSetCreatedAtAndSave() {
        SubProject sub = new SubProject();
        sub.setName("Test");
        sub.setProjectId(1L);

        when(subProjectRepository.save(any())).thenReturn(sub);

        SubProject result = subProjectService.createSubProject(sub);

        assertNotNull(result.getCreatedAt());
        verify(subProjectRepository).save(sub);
    }

    @Test
    void testCreateSubProject_shouldThrowExceptionWhenNameIsMissing() {
        SubProject sub = new SubProject();
        sub.setProjectId(1L);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                subProjectService.createSubProject(sub));
        assertEquals("Subprojekt navn er påkrævet", ex.getMessage());
    }

    @Test
    void testCreateSubProject_shouldThrowExceptionWhenProjectIdIsMissing() {
        SubProject sub = new SubProject();
        sub.setName("Test");

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                subProjectService.createSubProject(sub));
        assertEquals("Projekt ID er påkrævet", ex.getMessage());
    }

    @Test
    void testUpdateSubProject_shouldCallSave() {
        SubProject sub = new SubProject();
        when(subProjectRepository.save(sub)).thenReturn(sub);

        SubProject result = subProjectService.updateSubProject(sub);

        assertEquals(sub, result);
        verify(subProjectRepository).save(sub);
    }

    @Test
    void testDeleteSubProject_shouldCallDelete() {
        subProjectService.deleteSubProject(5L);

        verify(subProjectRepository).deleteById(5L);
    }

    @Test
    void testExistsByIdAndProjectId_shouldReturnTrue() {
        when(subProjectRepository.existsByIdAndProjectId(3L, 4L)).thenReturn(true);

        assertTrue(subProjectService.existsByIdAndProjectId(3L, 4L));
        verify(subProjectRepository).existsByIdAndProjectId(3L, 4L);
    }

    @Test
    void testGetProjectIdBySubProjectId_shouldReturnProjectId() {
        SubProject sub = new SubProject();
        sub.setProjectId(7L);
        when(subProjectRepository.findById(6L)).thenReturn(sub);

        Long result = subProjectService.getProjectIdBySubProjectId(6L);

        assertEquals(7L, result);
    }

    @Test
    void testGetProjectIdBySubProjectId_shouldReturnNullIfNotFound() {
        when(subProjectRepository.findById(6L)).thenReturn(null);

        Long result = subProjectService.getProjectIdBySubProjectId(6L);

        assertNull(result);
    }
}