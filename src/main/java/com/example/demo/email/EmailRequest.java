package com.example.demo.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Corpul cererii POST /email: destinatarii selectati, subiectul si mesajul.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private List<Integer> userIds;
    private String subject;
    private String message;
}
