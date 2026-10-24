package com.example.demo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabela app_user: copilul care foloseste aplicatia.
 * totalPoints creste cand AI-ul aproba o dovada foto pentru o sarcina casnica.
 * Campurile sunt Integer (nu int) ca sa poata lipsi din JSON-ul trimis de client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;
    private String name;
    private String email;
    private Integer totalPoints;
}
