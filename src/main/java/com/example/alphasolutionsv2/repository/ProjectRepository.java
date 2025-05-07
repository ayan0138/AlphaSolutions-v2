package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Project project) {
        String sql = "INSERT INTO Projects (name, description, start_date, end_date, created_by) " +
                "VALUES (?, ?, ?, ?, ?) ";
        jdbcTemplate.update(sql,
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy().getUserId()
        );
    }

    public List<Project> findUserById(Long userId) {
        String sql = """
        SELECT p.project_id, p.name, p.description, p.start_date, p.end_date, 
               p.created_by, p.created_at, r.role_name
        FROM Projects p
        JOIN Users u ON p.created_by = u.user_id
        JOIN Roles r ON u.role_id = r.role_id
        WHERE p.created_by = ?
        
        """;
        return jdbcTemplate.query(sql, new ProjectRowMapper(), userId);
    }
}
