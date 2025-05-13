package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.SubProject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubProjectRepository {
    private final JdbcTemplate jdbcTemplate;

        public SubProjectRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

    public boolean existsByIdAndProjectId(Long subProjectId, Long projectId) { //ændringer fra WHERE id til WHERE subproject
        String sql = "SELECT COUNT(*) FROM sub_projects WHERE sub_project_id = ? AND project_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, subProjectId, projectId);
        return count != null && count > 0;


    }
    // 🔽 Her tilføjer du metoden i forlængelse
    public List<SubProject> findAllByProjectId(Long projectId) {
        String sql = "SELECT * FROM sub_projects WHERE project_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SubProject subProject = new SubProject();
            subProject.setSubProjectId(rs.getLong("sub_project_id"));
            subProject.setName(rs.getString("name"));
            subProject.setDescription(rs.getString("description"));
            subProject.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            subProject.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            subProject.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            // Bemærk: Du kan også mappe project-id til et Project-objekt, hvis nødvendigt
            return subProject;
        }, projectId);
    }

    }

