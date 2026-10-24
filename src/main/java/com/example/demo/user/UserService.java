package com.example.demo.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Logica pentru utilizatori: validari simple + apeluri catre UserRepository.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Lista completa de utilizatori. */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Cauta utilizatorul sau raspunde cu 404. */
    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /** Creeaza un utilizator nou; numele si emailul sunt obligatorii. */
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and email are required");
        }
        return userRepository.save(user);
    }

    /** Actualizeaza numele si emailul unui utilizator existent. */
    public User update(int id, User user) {
        findById(id);
        user.setId(id);
        userRepository.update(user);
        return findById(id);
    }

    /** Sterge un utilizator existent. */
    public void delete(int id) {
        findById(id);
        userRepository.delete(id);
    }

    /** Returneaza destinatarii selectati pentru email; eroare daca lista este goala. */
    public List<User> requireByIds(List<Integer> ids) {
        List<User> users = userRepository.findByIds(ids);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one user");
        }
        return users;
    }

    /** Adauga puncte in urma aprobarii unei dovezi foto. */
    public void addPoints(int userId, int points) {
        userRepository.addPoints(userId, points);
    }
}
