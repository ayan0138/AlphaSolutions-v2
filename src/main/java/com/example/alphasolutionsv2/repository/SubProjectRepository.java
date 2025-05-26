package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.SubProject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
public class SubProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public SubProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubProject> findByProjectId(long projectId) {
        String sql = "SELECT * FROM sub_projects WHERE project_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SubProject.class), projectId);
    }

    public SubProject findById(long subProjectId) {
        try {
            String sql = "SELECT * FROM sub_projects WHERE sub_project_id = ?";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SubProject.class), subProjectId);
        } catch (Exception e) {
            return null;
        }
    }

    public SubProject save(SubProject subProject) {
        // Tjek om det er et nyt subprojekt (ID er null eller 0)
        if (subProject.getSubProjectId() == null || subProject.getSubProjectId() == 0) {
            // Insert new subproject
            String sql = "INSERT INTO sub_projects (project_id, name, description, start_date, end_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, subProject.getProjectId());
                ps.setString(2, subProject.getName());
                ps.setString(3, subProject.getDescription());
                ps.setObject(4, subProject.getStartDate());
                ps.setObject(5, subProject.getEndDate());
                ps.setTimestamp(6, subProject.getCreatedAt() != null ?
                        Timestamp.valueOf(subProject.getCreatedAt()) :
                        Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);

            // Sæt det genererede ID
            long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            subProject.setSubProjectId(generatedId);
        } else {
            // Update existing subproject
            String sql = "UPDATE sub_projects SET project_id = ?, name = ?, description = ?, " +
                    "start_date = ?, end_date = ? WHERE sub_project_id = ?";

            jdbcTemplate.update(sql,
                    subProject.getProjectId(),
                    subProject.getName(),
                    subProject.getDescription(),
                    subProject.getStartDate(),
                    subProject.getEndDate(),
                    subProject.getSubProjectId());
        }

        return subProject;
    }
    public void deleteById(long subProjectId) {
        String sql = "DELETE FROM sub_projects WHERE sub_project_id = ?";
        jdbcTemplate.update(sql, subProjectId);
    }

    public boolean existsByIdAndProjectId(long subProjectId, long projectId) {
        String sql = "SELECT COUNT(*) FROM sub_projects WHERE sub_project_id = ? AND project_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, subProjectId, projectId);
        return count != null && count > 0;
    }
    public Long getProjectIdBySubProjectId(long subProjectId) {
        // Ændret fra getSubProjectById til findById
        SubProject subProject = findById(subProjectId);
        return subProject != null ? subProject.getProjectId() : null;
    }
}
