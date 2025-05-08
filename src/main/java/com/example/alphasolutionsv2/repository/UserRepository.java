
package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByUsernameAndPassword(String username, String password) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            WHERE u.username = ? AND u.password = ?
        """;

        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), username, password);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public User findById(Long userId) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            WHERE u.user_id = ?
        """;

        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), userId);

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
        """;

        return jdbcTemplate.query(sql, new UserRowMapper());
    }
}