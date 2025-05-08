package com.example.alphasolutionsv2.repository;
import com.example.alphasolutionsv2.model.Project;
import com.example.alphasolutionsv2.model.Role;
import com.example.alphasolutionsv2.model.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectRowMapper implements RowMapper<Project> {
    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getLong("role_id"));
        role.setRoleName(rs.getString("role_name"));

        User user = new User();
        user.setUserId(rs.getLong("created_by"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password")); // sikkerhedsmæssigt: undgå helst passwords her
        user.setRole(role);

        Project project = new Project();
        project.setProjectId(rs.getLong("project_id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setStartDate(rs.getDate("start_date").toLocalDate());
        project.setEndDate(rs.getDate("end_date").toLocalDate());
        project.setCreatedBy(user);

        return project;
    }

}


