package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.SubProject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SubProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public SubProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByIdAndProjectId(Long subProjectId, Long projectId) {
        String sql = "SELECT COUNT(*) FROM sub_projects WHERE sub_project_id = ? AND project_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, subProjectId, projectId);
        return count != null && count > 0;
    }

    // Find all subprojects for a specific project
    public List<SubProject> findByProjectId(long projectId) {
        String sql = """
                SELECT sub_project_id as subProjectId, project_id as projectId, name, description, 
                       start_date as startDate, end_date as endDate, created_at as createdAt
                FROM sub_projects 
                WHERE project_id = ?
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SubProject.class), projectId);
    }

    // Find a specific subproject by ID
    public SubProject findById(long subProjectId) {
        String sql = """
                SELECT sub_project_id as subProjectId, project_id as projectId, name, description, 
                       start_date as startDate, end_date as endDate, created_at as createdAt
                FROM sub_projects 
                WHERE sub_project_id = ?
            """;
        List<SubProject> subProjects = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SubProject.class), subProjectId);
        return !subProjects.isEmpty() ? subProjects.get(0) : null;
    }

    // Save a subproject
    public SubProject save(SubProject subProject) {
        if (subProject.getSubProjectId() == 0) {
            // Insert new
            String sql = "INSERT INTO sub_projects (project_id, name, description, start_date, end_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            // Get the subProjectId first to ensure it's set correctly
            long subProjectId = jdbcTemplate.update(sql,
                    subProject.getProjectId(),
                    subProject.getName(),
                    subProject.getDescription(),
                    subProject.getStartDate(),
                    subProject.getEndDate(),
                    Timestamp.valueOf(subProject.getCreatedAt() != null ? subProject.getCreatedAt() : LocalDateTime.now())
            );

            // Get the generated ID and set it on the subProject
            Long generatedId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (generatedId != null) {
                subProject.setSubProjectId(generatedId);
            }
        } else {
            // Update existing
            String sql = "UPDATE sub_projects SET name = ?, description = ?, start_date = ?, end_date = ? " +
                    "WHERE sub_project_id = ?";
            jdbcTemplate.update(sql,
                    subProject.getName(),
                    subProject.getDescription(),
                    subProject.getStartDate(),
                    subProject.getEndDate(),
                    subProject.getSubProjectId()
            );
        }
        return subProject;
    }

    // Delete a subproject
    public void deleteById(long subProjectId) {
        String sql = "DELETE FROM sub_projects WHERE sub_project_id = ?";
        jdbcTemplate.update(sql, subProjectId);
    }
}
