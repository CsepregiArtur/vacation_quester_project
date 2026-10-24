package com.example.demo.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela submission: o dovada foto trimisa pentru o sarcina.
 * Contine verdictul AI-ului (feedback + status) si punctele acordate.
 * Obiectul este construit de server, deci nu are nevoie de constructor fara argumente la client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {

    private Integer id;
    private Integer taskId;
    private Integer userId;
    private String photoPath;
    private String feedback;
    private SubmissionStatus status;
    private Integer awardedPoints;
    private String createdOn;
}
