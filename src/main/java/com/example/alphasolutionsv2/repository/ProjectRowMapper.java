package com.example.alphasolutionsv2.repository;

import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectRowMapper implements RowMapper<Project> {

    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        Project project = new Project();

        project.setProjectId(rs.getLong("project_id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setStartDate(rs.getObject("start_date", LocalDate.class));
        project.setEndDate(rs.getObject("end_date", LocalDate.class));
        project.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

        // Brugerobjekt
        User user = new User();
        user.setUserId(rs.getLong("created_by"));

        // Rolle-objekt
        Role role = new Role();
        role.setRoleName(rs.getString("role_name"));

        user.setRole(role);
        project.setCreatedBy(user);

        return project;
    }
}

