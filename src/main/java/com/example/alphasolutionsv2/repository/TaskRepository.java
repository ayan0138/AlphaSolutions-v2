package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Task task) {
        String sql = "INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date, created_at, estimated_hours, hourly_rate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";  //tilføjelse af EstimatedHours og HourlyRate

        jdbcTemplate.update(sql,
                task.getSubProjectId(),
                task.getName(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getStatus(),
                task.getDueDate(),
                Timestamp.valueOf(task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now()),
                task.getEstimatedHours(),
                task.getHourlyRate()
        );
    }   }
