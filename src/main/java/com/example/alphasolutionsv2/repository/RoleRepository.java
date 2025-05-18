package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleRepository {
    private final JdbcTemplate jdbcTemplate;

    public  RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Role> findAll() {
        String sql = "SELECT role_id, role_name FROM roles";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Role(rs.getLong("role_id"), rs.getString("role_name")));
    }

    public Role findById(Long id) {
        String sql = "SELECT role_id, role_name FROM roles WHERE role_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Role(rs.getLong("role_id"), rs.getString("role_name")), id);
    }

    public void saveRole(Role role) {
        String checkSql = "SELECT COUNT(*) FROM roles WHERE role_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, role.getRoleId());

        if (count != null && count == 0) {
            String sql = "INSERT INTO Roles (role_id, role_name) VALUES (?, ?)";
            jdbcTemplate.update(sql, role.getRoleId(), role.getRoleName());
        }
    }
}
