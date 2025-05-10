package com.example.alphasolutionsv2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SubProjectRepository {
    private final JdbcTemplate jdbcTemplate;

        public SubProjectRepository(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

    public boolean existsByIdAndProjectId(long subProjectId, long projectId) { //true or false om subproject er under project via id
            String sql = "SELECT COUNT(*) FROM sub_projects WHERE id = ? AND project_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, subProjectId, projectId);
            return count != null && count > 0;
        }
    }

