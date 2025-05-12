package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        Role role = new Role(
        rs.getLong("role_id"),
        rs.getString("role_name")
                );

        return new User(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"), // Denne er kritisk for login!
                role
        );
    }
}