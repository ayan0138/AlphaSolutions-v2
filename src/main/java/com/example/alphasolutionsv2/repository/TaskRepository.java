package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Task;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Gem en opgave i databasen
    public void save(Task task) {
        String sql = """
        INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date, created_at, price)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(sql,
                task.getSubProjectId(),
                task.getProjectId(),
                task.getName(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getPrice()  // 💰 VIGTIGT!
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

    public BigDecimal calculateTotalPriceBySubprojectId(Long subProjectId) {
        String sql = """
        SELECT COALESCE(SUM(price), 0.00)
        FROM Tasks
        WHERE sub_project_id = ?
    """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, subProjectId);
    }

    public BigDecimal getTotalPriceForSubProject(Long subProjectId) {
        String sql = "SELECT COALESCE(SUM(price), 0.00) FROM tasks WHERE sub_project_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, subProjectId);
    }

    public List<Task> findBySubProjectId(Long subProjectId) {
        String sql = """
        SELECT task_id, sub_project_id, name, description, assigned_to,
               status, due_date, created_at, price,
               estimated_hours, hourly_rate
        FROM tasks
        WHERE sub_project_id = ?
    """;

        return jdbcTemplate.query(sql, new TaskRowMapper(), subProjectId);
    }
}
