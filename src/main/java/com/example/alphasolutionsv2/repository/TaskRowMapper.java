package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Task;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskRowMapper implements RowMapper<Task> {

    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getLong("task_id"));
        task.setSubProjectId(rs.getLong("sub_project_id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));
        task.setAssignedTo(rs.getObject("assigned_to") != null ? rs.getLong("assigned_to") : null);
        task.setStatus(rs.getString("status"));
        task.setDueDate(rs.getObject("due_date", LocalDate.class));
        task.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        task.setPrice(rs.getBigDecimal("price"));

        // Hent estimated_hours og hourly_rate fra databasen
        task.setEstimatedHours(rs.getDouble("estimated_hours"));
        task.setHourlyRate(rs.getDouble("hourly_rate"));

        return task;
    }
}
