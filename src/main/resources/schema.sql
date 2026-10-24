-- Schema bazei de date pentru Vacation Quester.
-- Ruleaza automat la fiecare pornire (spring.sql.init.mode=always) si este idempotenta.

PRAGMA foreign_keys = ON;

-- Utilizatorii aplicatiei (copilul). Punctele cresc cand AI-ul aproba o dovada foto.
CREATE TABLE IF NOT EXISTS app_user (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    email        TEXT    NOT NULL UNIQUE,
    total_points INTEGER NOT NULL DEFAULT 0
);

-- Lecturile: titlul este subiectul ales, continutul este generat de AI.
-- status: PENDING (asteapta aprobare) / PUBLISHED / REMOVED.
CREATE TABLE IF NOT EXISTS post (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    title      TEXT    NOT NULL,
    content    TEXT    NOT NULL,
    status     TEXT    NOT NULL DEFAULT 'PENDING'
               CHECK (status IN ('PENDING', 'PUBLISHED', 'REMOVED')),
    user_id    INTEGER NOT NULL,
    created_on TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

-- Intrebarile minitestului generat de AI pentru fiecare lectura.
CREATE TABLE IF NOT EXISTS quiz_question (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id        INTEGER NOT NULL,
    question       TEXT    NOT NULL,
    option_a       TEXT    NOT NULL,
    option_b       TEXT    NOT NULL,
    option_c       TEXT    NOT NULL,
    correct_option TEXT    NOT NULL CHECK (correct_option IN ('A', 'B', 'C')),
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
);

-- Comentariile lasate de utilizatori la lecturi.
CREATE TABLE IF NOT EXISTS comment (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id    INTEGER NOT NULL,
    content    TEXT    NOT NULL,
    user_id    INTEGER NOT NULL,
    created_on TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

-- Sarcinile casnice cu recompensa in puncte.
CREATE TABLE IF NOT EXISTS household_task (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title         TEXT    NOT NULL,
    description   TEXT    NOT NULL,
    reward_points INTEGER NOT NULL CHECK (reward_points > 0),
    active        INTEGER NOT NULL DEFAULT 1
);

-- Dovezile foto trimise pentru o sarcina, cu verdictul AI-ului.
CREATE TABLE IF NOT EXISTS submission (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id        INTEGER NOT NULL,
    user_id        INTEGER NOT NULL,
    photo_path     TEXT    NOT NULL,
    feedback       TEXT,
    status         TEXT    NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    awarded_points INTEGER NOT NULL DEFAULT 0,
    created_on     TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES household_task (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
