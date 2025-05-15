package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.SubProject;
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

    // Save a task to the database
    public void save(Task task) {
        String sql = "INSERT INTO tasks (sub_project_id, project_id, name, description, assigned_to, status, due_date, created_at, estimated_hours, hourly_rate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //glemte at implementere project_id i query, derfor den gav out of bound

        jdbcTemplate.update(sql,
                task.getSubProjectId(),
                task.getProjectId(),
                task.getName(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getStatus(),
                task.getDueDate(),
                Timestamp.valueOf(task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now()),
                task.getEstimatedHours(),
                task.getHourlyRate()
        );
    }

    // Update an existing task
    public void update(Task task) {
        String sql = """
                UPDATE tasks 
                SET sub_project_id = ?,project_id = ?, name = ?, description = ?, assigned_to = ?, 
                    status = ?, due_date = ?, estimated_hours = ?, hourly_rate = ?
                WHERE task_id = ?
            """;

        jdbcTemplate.update(sql,
                task.getSubProjectId(),
                task.getProjectId(),
                task.getName(),
                task.getDescription(),
                task.getAssignedTo(),
                task.getStatus(),
                task.getDueDate(),
                task.getEstimatedHours(),
                task.getHourlyRate(),
                task.getTaskId()
        );
    }

    // Get all tasks for a given project with subproject info.
    public List<Task> findTasksByProjectId(long projectId) {
        String sql = """
                SELECT t.task_id as taskId, t.sub_project_id as subProjectId, t.name, t.description, 
                       t.assigned_to as assignedTo, t.status, t.due_date as dueDate, t.created_at as createdAt, 
                       t.estimated_hours as estimatedHours, t.hourly_rate as hourlyRate,
                       sp.name as subProjectName, sp.project_id as subProjectProjectId
                FROM tasks t
                LEFT JOIN sub_projects sp ON t.sub_project_id = sp.sub_project_id
                WHERE sp.project_id = ? OR (t.sub_project_id IS NULL AND ? IS NOT NULL)
            """;

        List<Task> tasks = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Task task = new Task();
            task.setTaskId(rs.getLong("taskId"));
            task.setSubProjectId(rs.getLong("subProjectId"));
            task.setName(rs.getString("name"));
            task.setDescription(rs.getString("description"));
            task.setAssignedTo(rs.getLong("assignedTo"));
            task.setStatus(rs.getString("status"));
            task.setDueDate(rs.getDate("dueDate") != null ? rs.getDate("dueDate").toLocalDate() : null);
            task.setCreatedAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null);
            task.setEstimatedHours(rs.getDouble("estimatedHours"));
            task.setHourlyRate(rs.getDouble("hourlyRate"));

            // Set subproject name if available.
            String subProjectName = rs.getString("subProjectName");
            if (subProjectName != null) {
                SubProject subProject = new SubProject();
                subProject.setSubProjectId(rs.getLong("subProjectId"));
                subProject.setName(subProjectName);
                subProject.setProjectId(rs.getLong("subProjectProjectId"));
                task.setSubProject(subProject);
            }

            return task;
        }, projectId, projectId);

        return tasks;
    }

    // Get all tasks
    public List<Task> findAll() {
        String sql = """
                SELECT task_id as taskId, sub_project_id as subProjectId, name, description, 
                       assigned_to as assignedTo, status, due_date as dueDate, created_at as createdAt,
                       estimated_hours as estimatedHours, hourly_rate as hourlyRate
                FROM tasks
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class));
    }

    // Get a specific task by ID
    public Task findById(long taskId) {
        String sql = """
                SELECT task_id as taskId, sub_project_id as subProjectId, name, description, 
                       assigned_to as assignedTo, status, due_date as dueDate, created_at as createdAt,
                       estimated_hours as estimatedHours, hourly_rate as hourlyRate
                FROM tasks 
                WHERE task_id = ?
            """;
        List<Task> tasks = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class), taskId);
        return !tasks.isEmpty() ? tasks.get(0) : null;
    }

    // Delete a task
    public void deleteById(long taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        jdbcTemplate.update(sql, taskId);
    }
    public List<Task> findTasksBySubProjectId(long subProjectId) {
        String sql = """
            SELECT task_id as taskId, sub_project_id as subProjectId, name, description, 
                   assigned_to as assignedTo, status, due_date as dueDate, created_at as createdAt, 
                   estimated_hours as estimatedHours, hourly_rate as hourlyRate
            FROM tasks
            WHERE sub_project_id = ?
        """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class), subProjectId);
    }
}
