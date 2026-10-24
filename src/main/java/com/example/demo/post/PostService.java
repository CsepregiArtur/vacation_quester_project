package com.example.demo.post;

import com.example.demo.ai.GeminiService;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Logica pentru lecturi:
 * - crearea unei lecturi cere AI-ului textul si minitestul;
 * - aprobarea/respingerea schimba statusul (PENDING -> PUBLISHED / REMOVED).
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final QuizRepository quizRepository;
    private final UserService userService;
    private final GeminiService geminiService;

    public PostService(PostRepository postRepository, QuizRepository quizRepository,
                       UserService userService, GeminiService geminiService) {
        this.postRepository = postRepository;
        this.quizRepository = quizRepository;
        this.userService = userService;
        this.geminiService = geminiService;
    }

    /**
     * Creeaza o lectura noua: AI-ul scrie textul si cele 3 intrebari,
     * apoi lectura este salvata cu status PENDING (asteapta aprobare).
     */
    public Post create(Post post) {
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (post.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        User user = userService.findById(post.getUserId());

        GeminiService.Reading reading = geminiService.generateReading(post.getTitle(), user.getName());

        Post saved = postRepository.save(post.getTitle(), reading.text(), user.getId());
        for (GeminiService.Question question : reading.questions()) {
            quizRepository.save(saved.getId(), question.question(), question.optionA(),
                    question.optionB(), question.optionC(), question.correctOption());
        }
        return saved;
    }

    /** Toate lecturile existente. */
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    /** Cauta lectura sau raspunde cu 404. */
    public Post findById(int id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    /** Lecturile unui utilizator, indiferent de status. */
    public List<Post> findByUserId(int userId) {
        userService.findById(userId);
        return postRepository.findByUserId(userId);
    }

    /** CUSTOM QUERY 1: lecturile publicate ale unui utilizator. */
    public List<Post> findPublishedByUserId(int userId) {
        userService.findById(userId);
        return postRepository.findPublishedByUserId(userId);
    }

    /** Modifica titlul si textul unei lecturi. */
    public Post update(int id, Post post) {
        findById(id);
        postRepository.update(id, post.getTitle(), post.getContent());
        return findById(id);
    }

    /** Aproba lectura (PENDING -> PUBLISHED). */
    public Post approve(int id) {
        findById(id);
        postRepository.updateStatus(id, Status.PUBLISHED);
        return findById(id);
    }

    /** Elimina lectura (-> REMOVED). */
    public Post remove(int id) {
        findById(id);
        postRepository.updateStatus(id, Status.REMOVED);
        return findById(id);
    }

    /** Sterge definitiv lectura. */
    public void delete(int id) {
        findById(id);
        postRepository.delete(id);
    }

    /** Returneaza minitestul unei lecturi. */
    public List<QuizQuestion> findQuiz(int postId) {
        findById(postId);
        return quizRepository.findByPostId(postId);
    }
}
