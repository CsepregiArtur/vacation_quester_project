package com.example.demo.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela post: o lectura. Titlul este subiectul ales de copil, iar continutul
 * este textul generat de AI (Gemini) la creare.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Integer id;
    private String title;
    private String content;
    private String createdOn;
    private Status status;
    private Integer userId;
}
