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


    // Find alle projekter oprettet af en bestemt bruger
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

    // Find projekter tildelt en bruger (fra Project_Assignments table)
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

    // Hent alle projekter for en bruger (både oprettet af og tildelt)
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
                ORDER BY p.start_date ASC, p.end_date ASC
        
        """; //ASC i my-project
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId, userId);
    }

    // Find et projekt ud fra ID
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
                ORDER BY p.start_date ASC, p.end_date ASC
        """;

        List<Project> results = jdbcTemplate.query(sql, new ProjectRowMapper(), projectId);

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }

}

    // Metode til at opdatere et projekt
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

    // Tjek om en bruger har en rolle i projektet (f.eks. Manager, Editor)
    public boolean userHasRoleInProject(Long userId, Long projectId) {
        String sql = """
            SELECT COUNT(*) FROM Project_Assignments pa
            WHERE pa.user_id = ? AND pa.project_id = ? AND pa.project_role IN ('EDITOR', 'MANAGER')
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, projectId);
        return count != null && count > 0;
    }

    // Tjek om en bruger har adgang til projektet
    public boolean userHasAccessToProject(Long userId, Long projectId) {
        String sql = """
            SELECT COUNT(*) FROM Project_Assignments pa
            WHERE pa.user_id = ? AND pa.project_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, projectId);
        return count != null && count > 0;
    }
    public void deleteById(Long projectId) {
        // Slet først alle referencer i Project_Assignments table
        String deleteAssignmentsSql = "DELETE FROM Project_Assignments WHERE project_id = ?";
        jdbcTemplate.update(deleteAssignmentsSql, projectId);

        // Slet derefter selve projektet
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
    // Add this method to ProjectRepository
    public List<Project> findProjectsByAssignedTasks(Long userId) {
        String sql = """
        SELECT DISTINCT p.project_id, p.name, p.description, p.start_date, p.end_date,
               p.created_by, p.created_at,
               u.username, u.email, u.password,
               r.role_id, r.role_name
        FROM Projects p
        JOIN Users u ON p.created_by = u.user_id
        JOIN Roles r ON u.role_id = r.role_id
        JOIN Sub_Projects sp ON sp.project_id = p.project_id
        JOIN Tasks t ON t.sub_project_id = sp.sub_project_id
        WHERE t.assigned_to = ?
        ORDER BY p.start_date ASC, p.end_date ASC
    """;
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId);
    }
    public boolean userHasTasksInProject(Long userId, Long projectId) {
        String sql = """
        SELECT COUNT(*) FROM tasks t
        JOIN sub_projects sp ON t.sub_project_id = sp.sub_project_id
        WHERE sp.project_id = ? AND t.assigned_to = ?
    """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId, userId);
        return count != null && count > 0;
    }

}
