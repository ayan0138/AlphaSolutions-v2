package com.example.alphasolutionsv2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
    }

