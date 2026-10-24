-- Exemplu minim: poti sterge/schimba aceste tabele dupa modelul tau de date.
CREATE TABLE IF NOT EXISTS app_user (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    username   TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
