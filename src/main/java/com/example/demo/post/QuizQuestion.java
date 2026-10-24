package com.example.demo.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela quiz_question: o intrebare grila din minitestul generat de AI
 * pentru lectura cu id-ul postId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    private Integer id;
    private Integer postId;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String correctOption;
}
