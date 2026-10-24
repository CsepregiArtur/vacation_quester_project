package com.example.demo.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela household_task: o sarcina casnica (ex. "Aspirat in Camera & Hol")
 * cu recompensa in puncte. Daca active = false, nu mai poate primi dovezi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdTask {

    private Integer id;
    private String title;
    private String description;
    private Integer rewardPoints;
    private Boolean active;
}
