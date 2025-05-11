
package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Optional<User> findById(Long userId) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            WHERE u.user_id = ?
        """;

        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), userId);

        return results.stream().findFirst();
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
        """;

        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.password, r.role_name as role
                FROM Users u
                JOIN Roles r ON u.role_id = r.role_id
                WHERE u.username = ?
                """;
        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), username);
        return results.stream().findFirst();
    }
}