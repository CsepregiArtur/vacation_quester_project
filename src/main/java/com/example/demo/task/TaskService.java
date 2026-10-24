package com.example.demo.task;

import com.example.demo.ai.GeminiService;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Logica sarcinilor casnice si a dovezilor foto:
 * 1. copilul incarca o poza la o sarcina;
 * 2. AI-ul (Gemini Vision) o verifica si da un verdict;
 * 3. daca poza este aprobata, punctele sarcinii se adauga la totalul copilului.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final GeminiService geminiService;
    private final ProofStorage proofStorage;

    public TaskService(TaskRepository taskRepository, SubmissionRepository submissionRepository,
                       UserService userService, GeminiService geminiService, ProofStorage proofStorage) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.geminiService = geminiService;
        this.proofStorage = proofStorage;
    }

    /** Toate sarcinile casnice. */
    public List<HouseholdTask> findAll() {
        return taskRepository.findAll();
    }

    /** Cauta sarcina sau raspunde cu 404. */
    public HouseholdTask findById(int id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    /** Adauga o sarcina noua; titlul, descrierea si recompensa sunt obligatorii. */
    public HouseholdTask create(HouseholdTask task) {
        if (task.getTitle() == null || task.getTitle().isBlank()
                || task.getDescription() == null || task.getDescription().isBlank()
                || task.getRewardPoints() == null || task.getRewardPoints() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "title, description and a positive rewardPoints are required");
        }
        return taskRepository.save(task);
    }

    /** Actualizeaza o sarcina existenta. */
    public HouseholdTask update(int id, HouseholdTask task) {
        findById(id);
        task.setId(id);
        taskRepository.update(task);
        return findById(id);
    }

    /** Sterge o sarcina existenta. */
    public void delete(int id) {
        findById(id);
        taskRepository.delete(id);
    }

    /** Toate dovezile trimise pentru o sarcina. */
    public List<TaskSubmission> findSubmissions(int taskId) {
        findById(taskId);
        return submissionRepository.findByTaskId(taskId);
    }

    /**
     * Primeste poza, cere AI-ului sa o verifice si salveaza rezultatul.
     * Punctele se acorda doar daca AI-ul a aprobat poza.
     */
    public TaskSubmission submitProof(int taskId, int userId, MultipartFile photo) {
        HouseholdTask task = findById(taskId);
        User user = userService.findById(userId);
        if (Boolean.FALSE.equals(task.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is not active");
        }
        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo is required");
        }

        byte[] bytes;
        try {
            bytes = photo.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the photo");
        }

        String photoPath = proofStorage.save(photo);
        GeminiService.Proof proof = geminiService.checkProof(
                bytes, photo.getContentType(), task.getTitle(), task.getDescription(), user.getName());

        SubmissionStatus status = proof.approved() ? SubmissionStatus.APPROVED : SubmissionStatus.REJECTED;
        int points = proof.approved() ? task.getRewardPoints() : 0;
        if (points > 0) {
            userService.addPoints(userId, points);
        }
        return submissionRepository.save(taskId, userId, photoPath, proof.feedback(), status, points);
    }
}
