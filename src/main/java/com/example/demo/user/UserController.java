package com.example.demo.user;

import com.example.demo.post.Post;
import com.example.demo.post.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoint-urile pentru utilizatori (CRUD + o interogare custom).
 * Prefix: /user
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    /** POST /user - creeaza un utilizator nou. */
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    /** GET /user - lista tuturor utilizatorilor. */
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    /** GET /user/{id} - un singur utilizator. */
    @GetMapping("/{id}")
    public User findById(@PathVariable int id) {
        return userService.findById(id);
    }

    /** PUT /user/{id} - actualizeaza numele si emailul. */
    @PutMapping("/{id}")
    public User update(@PathVariable int id, @RequestBody User user) {
        return userService.update(id, user);
    }

    /** DELETE /user/{id} - sterge utilizatorul. */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        userService.delete(id);
    }

    /**
     * CUSTOM QUERY 1: GET /user/{id}/posts/published
     * Returneaza toate lecturile publicate ale unui utilizator (status = PUBLISHED).
     */
    @GetMapping("/{id}/posts/published")
    public List<Post> findPublishedPosts(@PathVariable int id) {
        return postService.findPublishedByUserId(id);
    }
}
