package com.example.demo.post;

/**
 * Statusul unei lecturi:
 * PENDING  - generata de AI, asteapta aprobarea unui parinte;
 * PUBLISHED - aprobata, apare in lista copilului;
 * REMOVED   - eliminata, nu mai este afisata.
 */
public enum Status {
    PENDING,
    PUBLISHED,
    REMOVED
}
