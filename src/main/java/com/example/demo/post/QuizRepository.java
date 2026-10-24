package com.example.demo.post;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accesul la tabela quiz_question (intrebarile minitestului unei lecturi).
 */
@Repository
public class QuizRepository {

    /** Transforma un rand din tabela in obiectul QuizQuestion. */
    private static final RowMapper<QuizQuestion> QUIZ_MAPPER = (rs, rowNum) -> new QuizQuestion(
            rs.getInt("id"),
            rs.getInt("post_id"),
            rs.getString("question"),
            rs.getString("option_a"),
            rs.getString("option_b"),
            rs.getString("option_c"),
            rs.getString("correct_option"));

    private final JdbcTemplate jdbcTemplate;

    public QuizRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Toate intrebarile unei lecturi, in ordinea in care au fost generate. */
    public List<QuizQuestion> findByPostId(int postId) {
        return jdbcTemplate.query(
                "SELECT * FROM quiz_question WHERE post_id = ? ORDER BY id",
                QUIZ_MAPPER, postId);
    }

    /** Salveaza o intrebare generata de AI. */
    public void save(int postId, String question, String optionA, String optionB,
                     String optionC, String correctOption) {
        jdbcTemplate.update("""
                        INSERT INTO quiz_question (post_id, question, option_a, option_b, option_c, correct_option)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                postId, question, optionA, optionB, optionC, correctOption);
    }
}
