package com.example.demo.comment;

import com.example.demo.post.PostService;
import com.example.demo.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Logica pentru comentarii: verifica existenta lecturii si a autorului,
 * apoi delega operatiile catre CommentRepository.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, PostService postService,
                          UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }

    /** Adauga un comentariu la o lectura existenta, scris de un utilizator existent. */
    public Comment create(Comment comment) {
        if (comment.getPostId() == null || comment.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId and userId are required");
        }
        postService.findById(comment.getPostId());
        userService.findById(comment.getUserId());
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        return commentRepository.save(comment);
    }

    /** CUSTOM QUERY 2: toate comentariile unei lecturi. */
    public List<Comment> findByPostId(int postId) {
        postService.findById(postId);
        return commentRepository.findByPostId(postId);
    }

    /** Sterge comentariul sau raspunde cu 404. */
    public void delete(int id) {
        if (commentRepository.delete(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
    }
}
