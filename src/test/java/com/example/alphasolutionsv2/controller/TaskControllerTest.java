package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.repository.TaskRepository;
import com.example.alphasolutionsv2.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Starter hele Spring Boot konteksten
@AutoConfigureMockMvc // (Bruges hvis du vil teste Controllers med MockMvc)
@Transactional // Sikrer at ændringer bliver rullet tilbage
@Rollback // Sørger for at databasen nulstilles efter testen
public class TaskControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testShouldSaveAndFetchTask() {
        // ----------- ARRANGE -----------
        // ex. subprojectId(1) hører til projectId(1) i test
        Task task = new Task();
        task.setName("Integration Opgave");
        task.setSubProjectId(1L); // Subprojekt 1 skal eksistere og høre til projekt 1 i testdata
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setEstimatedHours(8.0);
        task.setHourlyRate(450.0);

        // ----------- ACT -----------
        taskService.createTask(task, 1L); // Gem opgaven via servicelaget
        List<Task> tasks = taskRepository.findTasksByProjectId(1L); // Hent alle tasks for projektId = 1

        // ----------- ASSERT -----------
        assertFalse(tasks.isEmpty(), "Listen af opgaver bør ikke være tom");
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Integration Opgave")),
                "Opgaven med det forventede navn burde være til stede");
    }
}
