package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.config.location=classpath:/application-test.properties")
@AutoConfigureMockMvc
@Transactional // så testdata ryddes op bagefter
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Tester at oprettelse af gyldig opgave redirecter korrekt til success-side.
     */
    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void createTask_shouldRedirectToSuccess_whenValidInput() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(csrf())
                        .param("name", "Opgave A")
                        .param("projectId", "1")
                        .param("subProjectId", "1")
                        .param("estimatedHours", "5")
                        .param("hourlyRate", "450")
                        .param("dueDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tasks/project/1?success=true"));
    }

    /**
     * Tester at oprettelse med ugyldig input (fx tomt navn) redirecter til fejl-side.
     */
    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void createTask_shouldRedirectToError_whenNameMissing() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(csrf())
                        .param("name", "")
                        .param("projectId", "1")
                        .param("subProjectId", "1")
                        .param("estimatedHours", "5")
                        .param("hourlyRate", "450")
                        .param("dueDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tasks/project/1?error=*"));
    }

    /**
     * Tester at oprettelse fejler med fejlbesked, hvis subprojektet ikke tilhører det angivne projekt.
     */

    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void createTask_shouldRedirectToError_whenSubProjectMismatch() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(csrf())
                        .param("name", "Test")
                        .param("projectId", "999")
                        .param("subProjectId", "1")
                        .param("estimatedHours", "3")
                        .param("hourlyRate", "400")
                        .param("dueDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tasks/project/999?error=*"));
    }
}
