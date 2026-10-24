package com.example.demo.task;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoint-urile pentru sarcinile casnice si dovezile foto.
 * Prefix: /task
 */
@RestController
@RequestMapping("/task")
@CrossOrigin
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** GET /task - lista sarcinilor casnice. */
    @GetMapping
    public List<HouseholdTask> findAll() {
        return taskService.findAll();
    }

    /** GET /task/{id} - o sarcina. */
    @GetMapping("/{id}")
    public HouseholdTask findById(@PathVariable int id) {
        return taskService.findById(id);
    }

    /** POST /task - adauga o sarcina noua. */
    @PostMapping
    public HouseholdTask create(@RequestBody HouseholdTask task) {
        return taskService.create(task);
    }

    /** PUT /task/{id} - actualizeaza o sarcina. */
    @PutMapping("/{id}")
    public HouseholdTask update(@PathVariable int id, @RequestBody HouseholdTask task) {
        return taskService.update(id, task);
    }

    /** DELETE /task/{id} - sterge o sarcina. */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        taskService.delete(id);
    }

    /** GET /task/{id}/submissions - dovezile trimise pentru o sarcina. */
    @GetMapping("/{id}/submissions")
    public List<TaskSubmission> findSubmissions(@PathVariable int id) {
        return taskService.findSubmissions(id);
    }

    /**
     * POST /task/{id}/submissions?userId=1
     * Primeste poza (multipart, campul "photo"), o verifica prin Gemini Vision
     * si returneaza dovada cu verdictul si punctele acordate.
     */
    @PostMapping("/{id}/submissions")
    public TaskSubmission submitProof(@PathVariable int id,
                                      @RequestParam int userId,
                                      @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return taskService.submitProof(id, userId, photo);
    }
}
