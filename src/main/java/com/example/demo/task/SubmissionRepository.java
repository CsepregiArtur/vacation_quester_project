package com.example.demo.task;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Accesul la tabela submission (dovezile foto si verdictul AI-ului).
 */
@Repository
public class SubmissionRepository {

    /** Transforma un rand din tabela in obiectul TaskSubmission. */
    private static final RowMapper<TaskSubmission> SUBMISSION_MAPPER = (rs, rowNum) -> new TaskSubmission(
            rs.getInt("id"),
            rs.getInt("task_id"),
            rs.getInt("user_id"),
            rs.getString("photo_path"),
            rs.getString("feedback"),
            SubmissionStatus.valueOf(rs.getString("status")),
            rs.getInt("awarded_points"),
            rs.getString("created_on"));

    private final JdbcTemplate jdbcTemplate;

    public SubmissionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Toate dovezile unei sarcini, cele mai noi primele. */
    public List<TaskSubmission> findByTaskId(int taskId) {
        return jdbcTemplate.query(
                "SELECT * FROM submission WHERE task_id = ? ORDER BY created_on DESC, id DESC",
                SUBMISSION_MAPPER, taskId);
    }

    /** Cauta o dovada dupa id. */
    public Optional<TaskSubmission> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM submission WHERE id = ?", SUBMISSION_MAPPER, id)
                .stream()
                .findFirst();
    }

    /** Salveaza rezultatul verificarii AI pentru o poza. */
    public TaskSubmission save(int taskId, int userId, String photoPath, String feedback,
                               SubmissionStatus status, int awardedPoints) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO submission (task_id, user_id, photo_path, feedback, status, awarded_points)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, taskId);
            statement.setInt(2, userId);
            statement.setString(3, photoPath);
            statement.setString(4, feedback);
            statement.setString(5, status.name());
            statement.setInt(6, awardedPoints);
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().intValue()).orElseThrow();
    }
}
