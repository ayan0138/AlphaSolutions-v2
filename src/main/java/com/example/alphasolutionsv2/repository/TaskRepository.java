package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Task;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Gem en opgave i databasen
    public void save(Task task) {
        String sql = "INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"; // estimated_hours og hourly_rate mangler i SQL (7 paramatre istedet for 9)

        jdbcTemplate.update(sql,
                task.getSubProjectId(),
                task.getName(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getStatus(),
                task.getDueDate(),
                Timestamp.valueOf(task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now())
                //task.getEstimatedHours(), + estimated_hours er ikke implementeret i SQL
                //task.getHourlyRate(), + hourly_rate er ikke implementeret i SQL
        );
    }

    // Hent alle opgaver for et givent projekt
    public List<Task> findTasksByProjectId(long projectId) {
        String sql = """
                SELECT t.* 
                FROM tasks t
                JOIN sub_projects sp ON t.sub_project_id = sp.sub_project_id
                WHERE sp.project_id = ?
            """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class), projectId);
    }
}
