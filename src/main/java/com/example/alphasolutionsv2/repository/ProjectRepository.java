package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Gem et nyt projekt
    public void save(Project project) {
        String sql = """
        INSERT INTO Projects (name, description, start_date, end_date, created_by)
        VALUES (?, ?, ?, ?, ?)
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[] { "project_id" });
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setObject(3, project.getStartDate());
            ps.setObject(4, project.getEndDate());
            ps.setLong(5, project.getCreatedBy().getUserId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            long projectId = key.longValue();
            project.setProjectId(projectId);

            // Opret automatisk projektassignment
            if (project.getCreatedBy() != null && project.getCreatedBy().getUserId() != null) {
                String assignSql = """
                INSERT INTO Project_Assignments (project_id, user_id, project_role)
                VALUES (?, ?, 'OWNER')
            """;
                jdbcTemplate.update(assignSql, projectId, project.getCreatedBy().getUserId());
            }
        }
    }


    // Find all projects created by a specific user
    public List<Project> findProjectsCreatedByUser(Long userId) {
        String sql = """
                SELECT p.project_id, p.name, p.description, p.start_date, p.end_date,
                       p.created_by, p.created_at,
                       u.username, u.email, u.password,
                       r.role_id, r.role_name
                FROM Projects p
                JOIN Users u ON p.created_by = u.user_id
                JOIN Roles r ON u.role_id = r.role_id
                WHERE p.created_by = ?
        """;
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId);
    }

    // Find projects assigned to a user (from Project_Assignments table)
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

    // Get all projects for a user (both created by and assigned to)
    public List<Project> findAllProjectsForUser(Long userId) {
        String sql = """
                SELECT DISTINCT p.project_id, p.name, p.description, p.start_date, p.end_date,
                       p.created_by, p.created_at,
                       u.username, u.email, u.password,
                       r.role_id, r.role_name
                FROM Projects p
                JOIN Users u ON p.created_by = u.user_id
                JOIN Roles r ON u.role_id = r.role_id
                LEFT JOIN Project_Assignments pa ON p.project_id = pa.project_id
                WHERE p.created_by = ? OR pa.user_id = ?
        """;
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId, userId);
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
    public void deleteById(Long projectId) {
        // First delete all references in Project_Assignments table
        String deleteAssignmentsSql = "DELETE FROM Project_Assignments WHERE project_id = ?";
        jdbcTemplate.update(deleteAssignmentsSql, projectId);

        // Then delete the project itself
        String deleteProjectSql = "DELETE FROM Projects WHERE project_id = ?";
        jdbcTemplate.update(deleteProjectSql, projectId);
    }
    public List<Project> findAll() {
        String sql = """
            SELECT p.project_id, p.name, p.description, p.start_date, p.end_date,
                   p.created_by, p.created_at,
                   u.username, u.email, u.password,
                   r.role_id, r.role_name
            FROM Projects p
            JOIN Users u ON p.created_by = u.user_id
            JOIN Roles r ON u.role_id = r.role_id
            ORDER BY p.created_at DESC
    """;
        return jdbcTemplate.query(sql, new ProjectRowMapper());
    }
}
