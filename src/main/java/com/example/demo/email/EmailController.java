package com.example.demo.email;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint-ul pentru trimiterea de emailuri.
 * Prefix: /email
 */
@RestController
@RequestMapping("/email")
@CrossOrigin
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * POST /email - trimite mesajul catre utilizatorii selectati.
     * Exemplu: {"userIds":[1,2],"subject":"Recompensa","message":"Bravo!"}
     */
    @PostMapping
    public Map<String, Object> send(@RequestBody EmailRequest request) {
        int sent = emailService.send(request);
        return Map.of("recipients", sent);
    }
}
