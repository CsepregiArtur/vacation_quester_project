# vacation_quester_project
vacation_quester_project

AI 

CTIVITATE: LECTURĂ ZILNICĂ 📚
OPȚIONAL (+60 PCTS)
Alege ce vrei să citești astăzi. Gemini va compune un text unic, educativ și distractiv numai pentru tine, urmat de un minitest.

🛸 Spațiul și Sistemul Solar
Descopere secretele planetelor și ale găurilor negre.

🦁 Animale din Junglă
Află lucruri incredibile despre feline, elefanți și reptile.

🎮 Istoria Jocurilor Video
Cum s-au dezvoltat primele console și recorduri istorice.

💻 Cum funcționează un Robot?
Învață bazele codului și cum gândesc mașinile inteligente.

🐙 Creaturi din Adâncuri
Fă o scufundare în cel mai adânc loc de pe Pământ.

🏛️ Imperiul Roman
Despre gladiatori, invenții și cel mai mare imperiu din antichitate.
SAU SCRIE TEMA TA PREFERATĂ:

CREEAZĂ

Sarcini Casnice: Validare Automată cu Gemini Vision 👁️

Încarcă o dovadă (o fotografie cu munca ta gata terminată: camera curățată, patul făcut sau vasele strălucitoare). Robotul AI Gemini va inspecta inteligent imaginea și îți oferă feedback-ul și punctele pe loc!

Aspirat în Cameră & Hol
ÎN AȘTEPTARE
🏷️ HOUSEHOLD
🔥 STREAK: 3 ZILE
Aspiră cu atenție camera de jocuri, holul și spațiile adiacente.
RECOMPENSĂ
+70 Pcts
Trimite Dovadă Poză 📸
Curățenie în Cameră
ÎN AȘTEPTARE
🏷️ HOUSEHOLD
🔥 STREAK: 2 ZILE
Ordonează jucăriile primite în cutii, fă-ți patul excelent și pune cărțile pe rafturi.
RECOMPENSĂ
+60 Pcts
Trimite Dovadă Poză 📸
Aranjat și Clătit Vase
ÎN AȘTEPTARE
🏷️ HOUSEHOLD
🔥 STREAK: 1 ZI
Ajută-ți părinții clătind ușor farfuriile și așezându-le ordonat în dulapuri sau mașina de spălat.
RECOMPENSĂ
+50 Pcts
Trimite Dovadă Poză 📸
🧼 Igienă de Dimineață
ÎN AȘTEPTARE
🏷️ HOUSEHOLD
🔥 STREAK: 5 ZILE
Spală-te pe dinți timp de 2 minute, spală-te bine pe față, piaptănă-te și îmbracă-te cu haine curate de vacanță.
RECOMPENSĂ
+30 Pcts
Trimite Dovadă Poză 📸
🚿 Igienă de Seară
ÎN AȘTEPTARE
🏷️ HOUSEHOLD
🔥 STREAK: 4 ZILE
Duș sau baie revigorantă, igienă dentară de seară temeinică și așezat pijamale curate pentru somn.
RECOMPENSĂ
+30 Pcts
Trimite Dovadă Poză 📸

📁 Structură proiect (server)

Structura este pe pachete-functionale, la fel ca proiectul-exemplu de la curs, dar adaptata pentru Vacation Quester:

```text
src/main/java/com/example/demo/
├── DemoApplication.java      porneste Spring Boot
├── config/WebConfig.java     CORS pentru Vue + serveste pozele din uploads/
├── ai/GeminiService.java     AI-ul: text + minitest, respectiv validarea pozei (vision)
├── user/                     User, UserRepository, UserService, UserController
├── post/                     Post, Status, PostRepository, QuizQuestion, QuizRepository,
│                             PostService, PostController
├── comment/                  Comment, CommentRepository, CommentService, CommentController
├── task/                     HouseholdTask, TaskSubmission, SubmissionStatus,
│                             TaskRepository, SubmissionRepository, TaskService,
│                             TaskController, ProofStorage
└── email/                    EmailRequest, EmailService, EmailController
```

Fiecare pachet are aceeasi impartire ca in exemplu:
- `*Repository` - doar interogarile SQL (JdbcTemplate + SQLite);
- `*Service` - logica si validarile;
- `*Controller` - endpoint-urile REST.

🗄️ Baza de date

`src/main/resources/schema.sql` ruleaza automat la pornire:

| Tabelă | Rol |
|---|---|
| `app_user` | copilul (id, name, email, total_points) |
| `post` | lectura; `content` e scris de AI; status PENDING/PUBLISHED/REMOVED |
| `quiz_question` | cele 3 întrebări grilă generate de AI pentru o lectură |
| `comment` | comentariile copilului la o lectură |
| `household_task` | sarcina casnică + recompensa în puncte |
| `submission` | dovada foto + verdictul AI + punctele acordate |

Relații: `post.user_id` → `app_user`, `quiz_question.post_id` → `post`, `comment.post_id` → `post`,
`comment.user_id` → `app_user`, `submission.task_id` → `household_task`, `submission.user_id` → `app_user`
(toate cu `ON DELETE CASCADE`).

🤖 AI-ul din README (Gemini)

| Unde | Ce face |
|---|---|
| `POST /post` | Gemini scrie textul lecturii despre subiectul ales și 3 întrebări grilă (`GeminiService.generateReading`) |
| `POST /task/{id}/submissions` | Gemini Vision se uită la poza cu munca terminată și răspunde aprobat/respins + feedback (`GeminiService.checkProof`) |

Se folosește variabila de mediu `GEMINI_API_KEY` (modelul se poate schimba din `gemini.model`).
Fără cheie, aceste două endpoint-uri răspund cu `503` și mesaj clar.

🔌 Endpoint-uri

**Utilizatori** (`/user`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/user` | `createUser` |
| GET | `/user` | toți utilizatorii |
| GET | `/user/{id}` | un utilizator |
| PUT | `/user/{id}` | actualizare |
| DELETE | `/user/{id}` | ștergere |
| GET | `/user/{id}/posts/published` | **custom query**: lecturile publicate ale utilizatorului |

**Lecturi** (`/post`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/post` | creează lectura (AI) - pornește `PENDING` |
| GET | `/post` | toate lecturile |
| GET | `/post/{id}` | o lectură |
| PUT | `/post/{id}` | actualizează titlu + text |
| DELETE | `/post/{id}` | ștergere |
| POST | `/post/{id}/approve` | publică lectura |
| POST | `/post/{id}/remove` | elimină lectura |
| GET | `/post/{id}/quiz` | minitestul generat de AI |
| GET | `/post/{id}/comments` | **custom query**: comentariile lecturii |

**Comentarii** (`/comment`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/comment` | adaugă comentariu (`postId`, `userId`, `content`) |
| DELETE | `/comment/{id}` | ștergere |

**Sarcini casnice** (`/task`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/task` | creează sarcina |
| GET | `/task` | lista sarcinilor |
| GET | `/task/{id}` | o sarcină |
| PUT | `/task/{id}` | actualizare |
| DELETE | `/task/{id}` | ștergere |
| GET | `/task/{id}/submissions` | dovezile unei sarcini |
| POST | `/task/{id}/submissions?userId=` | încarcă poza (`multipart`, câmp `photo`), AI-ul o validează |

**Email** (`/email`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/email` | trimite email celor selectați |

▶️ Rulare

```bash
export GEMINI_API_KEY=cheia_ta     # obligatoriu pentru partea de AI
./mvnw spring-boot:run
```

Serverul pornește pe `http://localhost:8080`; baza `vacation_quester.db` și tabelele se creează automat (baza pornește goală).

Exemplu de flux complet:

```bash
# 1. utilizator
curl -X POST http://localhost:8080/user -H 'Content-Type: application/json' \
  -d '{"name":"Andrei","email":"andrei@example.com"}'

# 2. lectura generata de AI (status PENDING)
curl -X POST http://localhost:8080/post -H 'Content-Type: application/json' \
  -d '{"title":"Spatiul si Sistemul Solar","userId":1}'

# 3. aprobare -> apare in getPublishedPostsForUser
curl -X POST http://localhost:8080/post/1/approve

# 4. sarcina casnica + dovada foto validata de AI
curl -X POST http://localhost:8080/task -H 'Content-Type: application/json' \
  -d '{"title":"Aspirat in Camera & Hol","description":"Aspira camera si holul.","rewardPoints":70,"active":true}'
curl -X POST "http://localhost:8080/task/1/submissions?userId=1" -F "photo=@poza.jpg"

# 5. email catre cei selectati
curl -X POST http://localhost:8080/email -H 'Content-Type: application/json' \
  -d '{"userIds":[1],"subject":"Recompensa","message":"Ai primit 70 de puncte!"}'
```

Note:
- SMTP: completează `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`;
  fără ele, emailurile sunt doar scrise în log (endpoint-ul rămâne testabil).
- Pozele se salvează în `uploads/proofs/` și se pot descărca de la `/uploads/**`.
- Ce este implementat și ce nu: vezi [Project_requirements.md](Project_requirements.md).
