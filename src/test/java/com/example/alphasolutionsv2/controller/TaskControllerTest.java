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
    void testCreateTask_shouldRedirectToSuccess_onValidInput() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(csrf()) // CSRF-token kræves
                        .param("name", "Integrationstest")
                        .param("subProjectId", "1")
                        .param("estimatedHours", "10")
                        .param("hourlyRate", "500")
                        .param("dueDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/success"));
    }

    /**
     * Tester at oprettelse med ugyldig input (fx tomt navn) redirecter til fejl-side.
     */
    @Test
    @WithMockUser(username = "najib", roles = {"Medarbejder"})
    void testCreateTask_shouldRedirectToError_onInvalidInput() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .with(csrf())
                        .param("name", "") // tomt navn
                        .param("subProjectId", "1")
                        .param("estimatedHours", "10")
                        .param("hourlyRate", "500")
                        .param("dueDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/error"));
    }
}
