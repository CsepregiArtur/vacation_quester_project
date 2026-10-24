package com.example.demo.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Accesul la tabela app_user: operatii CRUD + adaugare de puncte.
 */
@Repository
public class UserRepository {

    /** Transforma un rand din tabela in obiectul User. */
    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getInt("total_points"));

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Returneaza toti utilizatorii. */
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM app_user ORDER BY id", USER_MAPPER);
    }

    /** Cauta un utilizator dupa id. */
    public Optional<User> findById(int id) {
        return jdbcTemplate.query("SELECT * FROM app_user WHERE id = ?", USER_MAPPER, id)
                .stream()
                .findFirst();
    }

    /** Returneaza utilizatorii cu id-urile date (folosit la trimiterea de email). */
    public List<User> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM app_user WHERE id IN (" + placeholders + ") ORDER BY id",
                USER_MAPPER,
                ids.toArray());
    }

    /** Insereaza un utilizator nou si returneaza obiectul cu id-ul generat. */
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO app_user (name, email) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            return statement;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        user.setTotalPoints(0);
        return user;
    }

    /** Actualizeaza numele si emailul unui utilizator. */
    public int update(User user) {
        return jdbcTemplate.update(
                "UPDATE app_user SET name = ?, email = ? WHERE id = ?",
                user.getName(), user.getEmail(), user.getId());
    }

    /** Sterge un utilizator; postarile, comentariile si dovezile lui se sterg in cascada. */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", id);
    }

    /** Adauga puncte (sau scade, cu valoare negativa), fara a cobori sub 0. */
    public int addPoints(int id, int points) {
        return jdbcTemplate.update(
                "UPDATE app_user SET total_points = MAX(0, total_points + ?) WHERE id = ?",
                points, id);
    }
}
