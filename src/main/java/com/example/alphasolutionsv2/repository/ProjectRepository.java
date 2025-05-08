package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a new project
    public void save(Project project) {
        String sql = "INSERT INTO Projects (name, description, start_date, end_date, created_by) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy().getUserId());
    }

    // Find projects by user ID
    public List<Project> findUserById(Long userId) {
        String sql = """
                SELECT p.project_id, p.name, p.description, p.start_date, p.end_date,
                       p.created_by, p.created_at,
                       u.username, u.email, u.password,
                       r.role_id, r.role_name
                FROM Projects p
                JOIN Users u ON p.created_by = u.user_id
                JOIN Roles r ON u.role_id = r.role_id
                JOIN Project_Assignments pa ON p.project_id = pa.project_id
                WHERE pa.user_id = ?
        """;
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId);
    }

    // Find a project by ID
    public Optional<Project> findById(Long projectId) {
        String sql = """
                SELECT p.project_id, p.name, p.description, p.start_date, p.end_date,
                       p.created_by, p.created_at,
                       u.username, u.email, u.password,
                       r.role_id, r.role_name
                FROM Projects p
                JOIN Users u ON p.created_by = u.user_id
                JOIN Roles r ON u.role_id = r.role_id
                WHERE p.project_id = ?
        """;

        List<Project> results = jdbcTemplate.query(sql, new ProjectRowMapper(), projectId);

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    // Method to update a project
    public void updateProject(Project project) {
        String sql = """
                UPDATE Projects
                SET name = ?, description = ?, start_date = ?, end_date = ?
                WHERE project_id = ?
        """;
        jdbcTemplate.update(sql,
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getProjectId());
    }

    // Check if a user has a role in the project (e.g., Editor, Manager)
    public boolean userHasRoleInProject(Long userId, Long projectId) {
        String sql = """
            SELECT COUNT(*) FROM Project_Assignments pa
            WHERE pa.user_id = ? AND pa.project_id = ? AND pa.project_role IN ('EDITOR', 'MANAGER')
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, projectId);
        return count != null && count > 0;
    }

    // Check if a user has access to the project
    public boolean userHasAccessToProject(Long userId, Long projectId) {
        String sql = """
            SELECT COUNT(*) FROM Project_Assignments pa
            WHERE pa.user_id = ? AND pa.project_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, projectId);
        return count != null && count > 0;
    }
}
