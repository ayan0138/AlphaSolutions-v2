package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.SubProject;
import com.example.alphasolutionsv2.model.Task;
import com.example.alphasolutionsv2.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
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
        String sql = "INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date, " +
                "created_at, estimated_hours, hourly_rate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
    }

    // Update an existing task
    public void update(Task task) {
        String sql = "UPDATE tasks SET name=?, description=?, status=?, due_date=?, " +
                "sub_project_id=?, estimated_hours=?, hourly_rate=?, assigned_to=? " +
                "WHERE task_id=?";

        jdbcTemplate.update(sql,
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getSubProjectId(),
                task.getEstimatedHours(),
                task.getHourlyRate(),
                task.getAssignedTo(),  // Sørg for at dette felt opdateres
                task.getTaskId()
        );

        // For debugging: Print et log for at se, hvad der gemmes
        System.out.println("Updating task " + task.getTaskId() + " with assignedTo: " + task.getAssignedTo());
    }
    // Get all tasks for a given project with subproject info.
    // I TaskRepository
    public List<Task> findTasksByProjectId(long projectId) {
        String sql = """
            SELECT t.task_id as taskId, t.sub_project_id as subProjectId, t.name, t.description, 
                   t.assigned_to as assignedTo, t.status, t.due_date as dueDate, t.created_at as createdAt, 
                   t.estimated_hours as estimatedHours, t.hourly_rate as hourlyRate,
                   sp.name as subProjectName, sp.project_id as subProjectProjectId,
                   u.user_id as assigned_user_id, u.username as assigned_username, u.email as assigned_email
            FROM tasks t
            LEFT JOIN sub_projects sp ON t.sub_project_id = sp.sub_project_id
            LEFT JOIN users u ON t.assigned_to = u.user_id
            WHERE sp.project_id = ? OR (t.sub_project_id IS NULL AND ? IS NOT NULL)
        """;

        List<Task> tasks = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Task task = new Task();
            task.setTaskId(rs.getLong("taskId"));
            task.setSubProjectId(rs.getLong("subProjectId"));
            task.setName(rs.getString("name"));
            task.setDescription(rs.getString("description"));
            task.setAssignedTo(rs.getObject("assignedTo") != null ? rs.getLong("assignedTo") : null);
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

            // Hent brugeroplysninger - DENNE DEL ER VIGTIGT AT TILFØJE
            if (rs.getObject("assigned_user_id") != null) {
                User user = new User();
                user.setUserId(rs.getLong("assigned_user_id"));
                user.setUsername(rs.getString("assigned_username"));
                user.setEmail(rs.getString("assigned_email"));
                task.setAssignedUser(user);
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
        String sql = "SELECT t.*, sp.name as subproject_name, sp.description as subproject_description, " +
                "sp.project_id, sp.start_date as subproject_start_date, sp.end_date as subproject_end_date, " +
                "u.user_id as assigned_user_id, u.username as assigned_username, u.email as assigned_email " +
                "FROM tasks t " +
                "LEFT JOIN sub_projects sp ON t.sub_project_id = sp.sub_project_id " +
                "LEFT JOIN users u ON t.assigned_to = u.user_id " +
                "WHERE t.task_id = ?";

        try {
            Task task = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Task t = new Task();
                t.setTaskId(rs.getLong("task_id"));
                t.setSubProjectId(rs.getLong("sub_project_id"));
                t.setName(rs.getString("name"));
                t.setDescription(rs.getString("description"));
                t.setAssignedTo(rs.getObject("assigned_to") != null ? rs.getLong("assigned_to") : null);
                t.setStatus(rs.getString("status"));
                t.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
                t.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                t.setEstimatedHours(rs.getDouble("estimated_hours"));
                t.setHourlyRate(rs.getDouble("hourly_rate"));

                // Opret SubProject-objekt hvis der er et subprojekt
                if (rs.getObject("subproject_name") != null) {
                    SubProject sp = new SubProject();
                    sp.setSubProjectId(rs.getLong("sub_project_id"));
                    sp.setName(rs.getString("subproject_name"));
                    sp.setDescription(rs.getString("subproject_description"));
                    sp.setProjectId(rs.getLong("project_id"));
                    // Sæt flere subprojekt felter efter behov
                    t.setSubProject(sp);
                }

                // Opret User-objekt hvis der er en tildelt bruger
                if (rs.getObject("assigned_user_id") != null) {
                    User u = new User();
                    u.setUserId(rs.getLong("assigned_user_id"));
                    u.setUsername(rs.getString("assigned_username"));
                    u.setEmail(rs.getString("assigned_email"));
                    // Sæt flere bruger felter efter behov
                    t.setAssignedUser(u);
                }

                return t;
            }, taskId);

            return task;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Delete a task
    public void deleteById(long taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        jdbcTemplate.update(sql, taskId);
    }
    public List<Task> findTasksBySubProjectId(long subProjectId) {
        String sql = """
        SELECT t.*,
               u.user_id as assigned_user_id, u.username as assigned_username, u.email as assigned_email
        FROM tasks t
        LEFT JOIN users u ON t.assigned_to = u.user_id
        WHERE t.sub_project_id = ?
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Task task = new Task();
            task.setTaskId(rs.getLong("task_id"));
            task.setSubProjectId(rs.getLong("sub_project_id"));
            task.setName(rs.getString("name"));
            task.setDescription(rs.getString("description"));
            task.setAssignedTo(rs.getObject("assigned_to") != null ? rs.getLong("assigned_to") : null);
            task.setStatus(rs.getString("status"));
            task.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
            task.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            task.setEstimatedHours(rs.getDouble("estimated_hours"));
            task.setHourlyRate(rs.getDouble("hourly_rate"));

            // Tilføj brugeroplysninger hvis der er tildelt en bruger
            if (rs.getObject("assigned_user_id") != null) {
                User user = new User();
                user.setUserId(rs.getLong("assigned_user_id"));
                user.setUsername(rs.getString("assigned_username"));
                user.setEmail(rs.getString("assigned_email"));
                task.setAssignedUser(user);
            }

            return task;
        }, subProjectId);
    }
}
