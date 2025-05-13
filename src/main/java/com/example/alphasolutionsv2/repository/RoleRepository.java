package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepository {
    private final JdbcTemplate jdbcTemplate;

    public  RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveRole(Role role) {
        String checkSql = "SELECT COUNT(*) FROM Roles WHERE role_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, role.getRoleId());

        if (count != null && count == 0) {
            String sql = "INSERT INTO Roles (role_id, role_name) VALUES (?, ?)";
            jdbcTemplate.update(sql, role.getRoleId(), role.getRoleName());
        }
    }
}
