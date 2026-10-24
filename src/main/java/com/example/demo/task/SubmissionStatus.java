package com.example.demo.task;

/**
 * Statusul unei dovezi foto:
 * PENDING  - asteapta verificarea (nu ar trebui sa apara daca AI-ul raspunde corect);
 * APPROVED - AI-ul a acceptat poza, punctele au fost acordate;
 * REJECTED - AI-ul nu a recunoscut munca terminata, nu se acorda puncte.
 */
public enum SubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
