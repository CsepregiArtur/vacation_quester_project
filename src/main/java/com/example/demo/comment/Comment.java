package com.example.demo.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela comment: un comentariu lasat de un utilizator la o lectura (post).
 * La creare se trimit doar postId, userId si content; id-ul il pune baza de date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Integer id;
    private Integer postId;
    private String content;
    private Integer userId;
    private String createdOn;
}
