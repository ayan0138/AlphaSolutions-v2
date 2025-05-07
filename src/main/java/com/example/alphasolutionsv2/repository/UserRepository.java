package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByUsernameAndPassword(String username, String password) {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.password, r.role_id, r.role_name
                FROM users u
                JOIN Roles r ON u.role_id = r.role_id
                WHERE u.username = ? AND u.password = ?  
                """;

        return jdbc.query(sql, rs -> {
            if(rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setUserName(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                Role role =  new Role();
                role.setRoleId(rs.getLong("role_id"));
                role.setRoleName(rs.getString("role_name"));

                user.setRole(role);
                return user;
            }
            return null;
        }, username, password);
    }




}
