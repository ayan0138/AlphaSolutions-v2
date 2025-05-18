
package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserRepository(JdbcTemplate jdbcTemplate, BCryptPasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    // Task 8.1 - Bruges til at oprette nye brugere
    public void createUser(User user) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getUsername());

        if(count != null && count == 0) {
            String sql = """
                    INSERT INTO users (username, email, password, role_id) 
                    VALUES (?, ?, ?, ?)
                    """;

            jdbcTemplate.update(sql,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole().getRoleId()
                    );
        }

    }

    public Optional<User> findById(Long userId) {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password, 
                   r.role_id, r.role_name
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            WHERE u.user_id = ?
        """;

        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), userId);

        return results.stream().findFirst();
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.user_id, u.username, u.email, u.password,
                   r.role_id, r.role_name 
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
        """;

        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.password,
                       r.role_id, r.role_name
                FROM Users u
                JOIN Roles r ON u.role_id = r.role_id
                WHERE u.username = ?
                """;
        List<User> results = jdbcTemplate.query(sql, new UserRowMapper(), username);
        return results.stream().findFirst();
    }

    // Bruges til at opdatere eksisterende bruger (fx hvis password ændres)
    public void saveUser(User user) {
        String sql = """
        UPDATE users 
        SET username = ?, email = ?, password = ?, role_id = ?
        WHERE user_id = ?
    """;

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // allerede hashed
                user.getRole().getRoleId(),
                user.getUserId() // korrekt primærnøgle
        );
    }
}