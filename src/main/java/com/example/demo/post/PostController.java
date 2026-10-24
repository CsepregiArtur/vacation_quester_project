package com.example.demo.post;

import com.example.demo.comment.Comment;
import com.example.demo.comment.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoint-urile pentru lecturi (CRUD + approve/remove + minitest + o interogare custom).
 * Prefix: /post
 */
@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    /** POST /post - creeaza lectura; AI-ul scrie textul si minitestul (status PENDING). */
    @PostMapping
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    /** GET /post - toate lecturile. */
    @GetMapping
    public List<Post> findAll() {
        return postService.findAll();
    }

    /** GET /post/{id} - o lectura. */
    @GetMapping("/{id}")
    public Post findById(@PathVariable int id) {
        return postService.findById(id);
    }

    /** PUT /post/{id} - actualizeaza titlul si textul. */
    @PutMapping("/{id}")
    public Post update(@PathVariable int id, @RequestBody Post post) {
        return postService.update(id, post);
    }

    /** DELETE /post/{id} - sterge lectura. */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        postService.delete(id);
    }

    /** POST /post/{id}/approve - publica lectura (PENDING -> PUBLISHED). */
    @PostMapping("/{id}/approve")
    public Post approve(@PathVariable int id) {
        return postService.approve(id);
    }

    /** POST /post/{id}/remove - elimina lectura (-> REMOVED). */
    @PostMapping("/{id}/remove")
    public Post remove(@PathVariable int id) {
        return postService.remove(id);
    }

    /** GET /post/{id}/quiz - intrebarile minitestului generat de AI. */
    @GetMapping("/{id}/quiz")
    public List<QuizQuestion> findQuiz(@PathVariable int id) {
        return postService.findQuiz(id);
    }

    /**
     * CUSTOM QUERY 2: GET /post/{id}/comments
     * Returneaza toate comentariile unei lecturi.
     */
    @GetMapping("/{id}/comments")
    public List<Comment> findComments(@PathVariable int id) {
        return commentService.findByPostId(id);
    }
}
