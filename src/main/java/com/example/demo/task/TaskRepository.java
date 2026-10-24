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
 * Accesul la tabela household_task (sarcinile casnice predefinite).
 */
@Repository
public class TaskRepository {

    /** Transforma un rand din tabela in obiectul HouseholdTask. */
    private static final RowMapper<HouseholdTask> TASK_MAPPER = (rs, rowNum) -> new HouseholdTask(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getInt("reward_points"),
            rs.getBoolean("active"));

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Toate sarcinile. */
    public List<HouseholdTask> findAll() {
        return jdbcTemplate.query("SELECT * FROM household_task ORDER BY id", TASK_MAPPER);
    }

    /** Cauta o sarcina dupa id. */
    public Optional<HouseholdTask> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM household_task WHERE id = ?", TASK_MAPPER, id)
                .stream()
                .findFirst();
    }

    /** Adauga o sarcina noua. */
    public HouseholdTask save(HouseholdTask task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO household_task (title, description, reward_points, active) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setInt(3, task.getRewardPoints());
            statement.setBoolean(4, task.getActive());
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().intValue()).orElseThrow();
    }

    /** Actualizeaza o sarcina existenta. */
    public int update(HouseholdTask task) {
        return jdbcTemplate.update(
                "UPDATE household_task SET title = ?, description = ?, reward_points = ?, active = ? WHERE id = ?",
                task.getTitle(), task.getDescription(), task.getRewardPoints(), task.getActive(), task.getId());
    }

    /** Sterge o sarcina; dovezile ei se sterg in cascada. */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM household_task WHERE id = ?", id);
    }
}
