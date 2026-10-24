# Project requirements

Aplicatie: **Vacation Quester** - lectura zilnica generata de AI + sarcini casnice validate de AI.

## Cerinte

Database: minim 3-4 tabele cu 3-4 coloane fiecare + relationare intre tabele. De exemplu:
```
User: id, name, email
Post: id, title, content, user_id, created_on, status (status poate fi PENDING, PUBLISHED, REMOVED)
Comment: id, post_id, content, user_id, created_on
```

Server:
- 8 endpoint-uri pentru operatiile CRUD (de exemplu, pe tabelele pe Post si Comment + createUser())
- 2 endpoint-uri cu custom query (ex: pentru getAllPublishedPostsForUser() si getAllCommentsForPost())

Frontend:
- sa permita minim 7 operatii operatii, de exemplu: create new post (pending), view posts and comments, view pending posts, add/update comment, remove post, etc.
- minim 4 componente Vue

Desktop/mobile client:
- sa permita minim 3 operatii: get all posts/comments containing keyword, approve/remove post, sendEmail to all selected persons (functionalitatea de sendEmail este inca un endpoint din server, se implementeaza usor cu SMTP, exista cod pe net)

Documentatie:
- diagrama use-case
- diagrama secventiala pentru un caz de utilizare
- nimic tiparit!

Other notes
- Proiectul trebuie sa fie consistent (utilizare posibila fara alterarea manuala a bazei de date: ex: getPublishedPosts() nu are sens fara approvePost())
- 1p in plus pentru prezentarea inainte de vacanta

---

## Stadiu implementare (server)

### ✅ Database - IMPLEMENTAT

Fisier: `src/main/resources/schema.sql` (rulat automat la pornire).

| Tabela | Coloane | Relationare |
|---|---|---|
| `app_user` | id, name, email, total_points | - |
| `post` | id, title, content, status, user_id, created_on | `user_id` → `app_user.id` |
| `quiz_question` | id, post_id, question, option_a, option_b, option_c, correct_option | `post_id` → `post.id` |
| `comment` | id, post_id, content, user_id, created_on | `post_id` → `post.id`, `user_id` → `app_user.id` |
| `household_task` | id, title, description, reward_points, active | - |
| `submission` | id, task_id, user_id, photo_path, feedback, status, awarded_points, created_on | `task_id` → `household_task.id`, `user_id` → `app_user.id` |

Statusuri: `post.status` = PENDING / PUBLISHED / REMOVED; `submission.status` = PENDING / APPROVED / REJECTED.

### ✅ Server - IMPLEMENTAT

**CRUD (mai mult de 8 endpoint-uri):**

| # | Metoda | Endpoint | Ce face |
|---|---|---|---|
| 1 | POST | `/user` | createUser (cerinta explicita) |
| 2 | GET | `/user` | lista utilizatorilor |
| 3 | GET | `/user/{id}` | un utilizator |
| 4 | PUT | `/user/{id}` | update utilizator |
| 5 | DELETE | `/user/{id}` | delete utilizator |
| 6 | POST | `/post` | creeaza lectura (AI scrie textul + minitestul) |
| 7 | GET | `/post` | lista lecturilor |
| 8 | GET | `/post/{id}` | o lectura |
| 9 | PUT | `/post/{id}` | update lectura |
| 10 | DELETE | `/post/{id}` | delete lectura |
| 11 | POST | `/comment` | adauga comentariu |
| 12 | DELETE | `/comment/{id}` | sterge comentariu |
| 13 | POST | `/task` | creeaza sarcina casnica |
| 14 | GET | `/task` | lista sarcinilor |
| 15 | GET | `/task/{id}` | o sarcina |
| 16 | PUT | `/task/{id}` | update sarcina |
| 17 | DELETE | `/task/{id}` | delete sarcina |

**Custom queries (2, cerinta):**

| # | Metoda | Endpoint | Interogarea |
|---|---|---|---|
| 1 | GET | `/user/{id}/posts/published` | toate lecturile publicate ale unui utilizator (`WHERE user_id = ? AND status = 'PUBLISHED'`) |
| 2 | GET | `/post/{id}/comments` | toate comentariile unei lecturi (`WHERE post_id = ?` + JOIN pe autor) |

**Endpoint-uri suplimentare (cerute de functionalitatile din README):**

| Metoda | Endpoint | Ce face |
|---|---|---|
| POST | `/post/{id}/approve` | aproba lectura (PENDING → PUBLISHED) |
| POST | `/post/{id}/remove` | elimina lectura (→ REMOVED) |
| GET | `/post/{id}/quiz` | intrebarile minitestului generat de AI |
| GET | `/task/{id}/submissions` | dovezile foto ale unei sarcini |
| POST | `/task/{id}/submissions?userId=` | upload poza + validare AI (Gemini Vision) + puncte |
| POST | `/email` | sendEmail catre persoanele selectate (SMTP) |

### ✅ AI (Gemini) - IMPLEMENTAT

Fisier: `src/main/java/com/example/demo/ai/GeminiService.java`

- `generateReading(topic, userName)` - la `POST /post`, Gemini scrie textul lecturii si 3 intrebari grila, salvate in `post` + `quiz_question`.
- `checkProof(photo, ...)` - la `POST /task/{id}/submissions`, Gemini Vision analizeaza poza si raspunde aprobat/respins + feedback; daca e aprobata, `reward_points` se adauga la `app_user.total_points`.

Necesita variabila de mediu `GEMINI_API_KEY`. Fara ea, endpoint-urile care folosesc AI raspund cu `503` si mesaj explicit (nu exista fallback/demo).

### ✅ Consistente - IMPLEMENTAT

- O lectura se creeaza mereu `PENDING` si apare in `getPublishedPostsForUser` doar dupa `approve`.
- Punctele se acorda doar la aprobarea AI a dovezii foto.
- `DELETE` pe utilizator / lectura / sarcina sterge in cascada comentariile, intrebarile si dovezile (chei straine cu `ON DELETE CASCADE`).

### ❌ Frontend (Vue, minim 4 componente, minim 7 operatii) - NEIMPLEMENTAT

Momentan exista doar serverul. Ecranele din README (alegere subiect lectura, lista sarcini cu "Trimite Dovada Poza") trebuie facute in Vue.

### ❌ Desktop / mobile client (minim 3 operatii) - NEIMPLEMENTAT

De facut: cautare dupa cuvant cheie, approve/remove lectura, sendEmail. Endpoint-urile exista deja.

### ❌ Documentatie (diagrama use-case, diagrama secventiala) - NEIMPLEMENTAT

### Observatii

- Baza de date este creata automat la prima pornire si este goala (fara date demo). Populare: `POST /user`, `POST /task`, apoi `POST /post`.
- Pozele doveditoare se salveaza in `uploads/proofs/` si se vad la `/uploads/**`.
- SMTP este optional: daca `spring.mail.host` lipseste, emailurile sunt doar scrise in log.
