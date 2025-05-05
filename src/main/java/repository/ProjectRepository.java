package repository;

import model.Project;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Project project) {
        String sql = "INSERT INTO projects (name, description, start_date, end_date, created_by) " +
                "VALUES (?, ?, ?, ?, ?) ";
        jdbcTemplate.update(sql,
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy()
        );
    }

    public List<Project>findByCreatedBy(long userId){
        String sql = "SELECT * FROM projects WHERE created_by = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Project.class), userId);
    }

}
