package com.example.demo.comment;

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
 * Accesul la tabela comment: adaugare, stergere si interogarea custom
 * "toate comentariile unui post".
 */
@Repository
public class CommentRepository {

    /** Transforma un rand din tabela in obiectul Comment. */
    private static final RowMapper<Comment> COMMENT_MAPPER = (rs, rowNum) -> new Comment(
            rs.getInt("id"),
            rs.getInt("post_id"),
            rs.getString("content"),
            rs.getInt("user_id"),
            rs.getString("created_on"));

    private final JdbcTemplate jdbcTemplate;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** CUSTOM QUERY 2: toate comentariile unei lecturi, in ordine cronologica. */
    public List<Comment> findByPostId(int postId) {
        return jdbcTemplate.query(
                "SELECT * FROM comment WHERE post_id = ? ORDER BY created_on, id",
                COMMENT_MAPPER, postId);
    }

    /** Cauta un comentariu dupa id. */
    public Optional<Comment> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM comment WHERE id = ?", COMMENT_MAPPER, id)
                .stream()
                .findFirst();
    }

    /** Salveaza un comentariu nou si il returneaza cu id-ul generat. */
    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO comment (post_id, content, user_id) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, comment.getPostId());
            statement.setString(2, comment.getContent());
            statement.setInt(3, comment.getUserId());
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().intValue()).orElseThrow();
    }

    /** Sterge un comentariu. */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM comment WHERE id = ?", id);
    }
}
