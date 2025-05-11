package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Create a Role object first
        Role role = new Role();
        try {
            // If you have role_id and role_name columns in your result set
            role.setRoleId(rs.getLong("role_id"));
            role.setRoleName(rs.getString("role_name"));
        } catch (SQLException e) {
            // If role columns don't exist or are null, create a role from the string
            String roleName = rs.getString("role");
            if (roleName != null) {
                role.setRoleName(roleName);
            }
        }

        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        // Note: For security reasons, you might want to avoid setting the password
      user.setPassword(rs.getString("password")); //husk at udkommentere tilbage igen
        user.setRole(role);

        return user;
    }
}