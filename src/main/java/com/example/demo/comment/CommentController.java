package com.example.demo.comment;

import org.springframework.web.bind.annotation.*;

/**
 * Endpoint-urile pentru comentarii (adaugare si stergere).
 * Listarea comentariilor unei lecturi se face prin GET /post/{id}/comments.
 * Prefix: /comment
 */
@RestController
@RequestMapping("/comment")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** POST /comment - adauga un comentariu (postId, userId, content). */
    @PostMapping
    public Comment create(@RequestBody Comment comment) {
        return commentService.create(comment);
    }

    /** DELETE /comment/{id} - sterge un comentariu. */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        commentService.delete(id);
    }
}
