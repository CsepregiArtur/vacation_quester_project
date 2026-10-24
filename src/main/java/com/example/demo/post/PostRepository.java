package com.example.demo.post;

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
 * Accesul la tabela post: CRUD, filtrare dupa utilizator si interogarea custom
 * pentru lecturile publicate.
 */
@Repository
public class PostRepository {

    /** Transforma un rand din tabela in obiectul Post. */
    private static final RowMapper<Post> POST_MAPPER = (rs, rowNum) -> new Post(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("created_on"),
            Status.valueOf(rs.getString("status")),
            rs.getInt("user_id"));

    private final JdbcTemplate jdbcTemplate;

    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Toate lecturile, cele mai noi primele. */
    public List<Post> findAll() {
        return jdbcTemplate.query("SELECT * FROM post ORDER BY created_on DESC, id DESC", POST_MAPPER);
    }

    /** Cauta o lectura dupa id. */
    public Optional<Post> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM post WHERE id = ?", POST_MAPPER, id)
                .stream()
                .findFirst();
    }

    /** Toate lecturile unui copil, indiferent de status. */
    public List<Post> findByUserId(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM post WHERE user_id = ? ORDER BY created_on DESC, id DESC",
                POST_MAPPER, userId);
    }

    /** CUSTOM QUERY 1: doar lecturile publicate ale unui copil. */
    public List<Post> findPublishedByUserId(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM post WHERE user_id = ? AND status = 'PUBLISHED' ORDER BY created_on DESC, id DESC",
                POST_MAPPER, userId);
    }

    /** Insereaza o lectura noua (statusul default din baza este PENDING). */
    public Post save(String title, String content, int userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO post (title, content, user_id) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setInt(3, userId);
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().intValue()).orElseThrow();
    }

    /** Actualizeaza titlul si continutul unei lecturi. */
    public int update(int id, String title, String content) {
        return jdbcTemplate.update(
                "UPDATE post SET title = ?, content = ? WHERE id = ?",
                title, content, id);
    }

    /** Schimba statusul (folosit la approve/remove). */
    public int updateStatus(int id, Status status) {
        return jdbcTemplate.update("UPDATE post SET status = ? WHERE id = ?", status.name(), id);
    }

    /** Sterge o lectura; intrebarile si comentariile se sterg in cascada. */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM post WHERE id = ?", id);
    }
}
