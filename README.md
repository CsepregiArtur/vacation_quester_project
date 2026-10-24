# Vacation Quester

Aplicație educativă de vacanță pentru copii: în fiecare zi poți citi o **lectură scrisă de AI**
(însoțită de un minitest grilă) și poți trimite **dovezi foto** pentru sarcini casnice, pe care
AI-ul le verifică și le răsplătește cu puncte.

Repository-ul conține momentan **serverul** (Spring Boot + SQLite); frontend-ul Vue și clientul
desktop/mobile urmează (vezi [Project_requirements.md](Project_requirements.md)).

## ✨ Funcționalități

**📚 Activitate: lectură zilnică (opțional, +60 pct)**
Alege ce vrei să citești astăzi. Gemini compune un text unic, educativ și distractiv numai pentru
tine, urmat de un minitest.

Subiecte propuse:

- 🛸 **Spațiul și Sistemul Solar** - descoperă secretele planetelor și ale găurilor negre.
- 🦁 **Animale din Junglă** - află lucruri incredibile despre feline, elefanți și reptile.
- 🎮 **Istoria Jocurilor Video** - cum s-au dezvoltat primele console și recorduri istorice.
- 💻 **Cum funcționează un Robot?** - învață bazele codului și cum gândesc mașinile inteligente.
- 🐙 **Creaturi din Adâncuri** - fă o scufundare în cel mai adânc loc de pe Pământ.
- 🏛️ **Imperiul Roman** - despre gladiatori, invenții și cel mai mare imperiu din antichitate.

Sau poți scrie propria temă preferată.

**👁️ Sarcini casnice: validare automată cu Gemini Vision**
Încarcă o dovadă (o fotografie cu munca ta gata terminată: camera curățată, patul făcut sau vasele
strălucitoare). Robotul AI Gemini inspectează inteligent imaginea și îți oferă feedback-ul și
punctele pe loc.

| Sarcină | Recompensă |
|---|---|
| Aspirat în Cameră & Hol | +70 pct |
| Curățenie în Cameră | +60 pct |
| Aranjat și Clătit Vase | +50 pct |
| 🧼 Igienă de Dimineață | +30 pct |
| 🚿 Igienă de Seară | +30 pct |

**Alte funcționalități:** comentarii la lecturi, aprobare/eliminare lecturi (moderare), punctaj
total pe utilizator, trimitere de emailuri către utilizatorii selectați.

## 🧰 Tehnologii

| Componentă | Detaliu |
|---|---|
| Limbaj | Java 25 |
| Framework | Spring Boot 4.1.2 (Web MVC, JDBC, Mail) |
| Bază de date | SQLite (`vacation_quester.db`), schema rulată automat la pornire |
| AI | Google Gemini (`gemini-2.5-flash`) - text, minitest și validare de imagini |
| Build | Maven (wrapper inclus: `./mvnw`) |

## 📁 Structură proiect (server)

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

## 🗄️ Baza de date

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

## 🤖 AI (Gemini)

| Unde | Ce face |
|---|---|
| `POST /post` | Gemini scrie textul lecturii despre subiectul ales și 3 întrebări grilă (`GeminiService.generateReading`) |
| `POST /task/{id}/submissions` | Gemini Vision se uită la poza cu munca terminată și răspunde aprobat/respins + feedback (`GeminiService.checkProof`) |

Configurare (`src/main/resources/application.properties`):

| Proprietate | Valoare implicită | Rol |
|---|---|---|
| `gemini.api-key` | `${GEMINI_API_KEY:}` | cheia API (din variabila de mediu) |
| `gemini.model` | `gemini-2.5-flash` | modelul folosit |
| `gemini.url` | `https://generativelanguage.googleapis.com` | endpoint-ul API |

Fără cheie, cele două endpoint-uri răspund cu `503` și mesaj clar (nu există fallback/demo).

## 🔌 Endpoint-uri

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
| POST | `/post` | creează lectura: AI scrie `content` + minitestul; pornește `PENDING` |
| GET | `/post` | toate lecturile, indiferent de status |
| GET | `/post/{id}` | o lectură |
| PUT | `/post/{id}` | actualizează titlu + text |
| DELETE | `/post/{id}` | ștergere |
| POST | `/post/{id}/approve` | publică lectura |
| POST | `/post/{id}/remove` | elimină lectura |
| GET | `/post/{id}/quiz` | minitestul generat de AI |
| POST | `/post/{id}/comments` | **custom query**: comentariile lecturii |

La creare se trimit doar `title` (subiectul ales) și `userId`; `content` și întrebările sunt
generate de AI. Statusurile unei lecturi: `PENDING` → `PUBLISHED` (`approve`) sau
`PENDING` → `REMOVED` (`remove`).

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
| POST | `/task/{id}/submissions?userId=` | încarcă poza (`multipart`, câmp `photo`: JPG/JPEG/PNG/WEBP, max 10 MB); AI-ul o validează și, dacă e aprobată, adaugă punctele la utilizator |

**Email** (`/email`)
| Metodă | Path | Ce face |
|---|---|---|
| POST | `/email` | trimite email celor selectați; răspunde cu `{"recipients": <număr>}` |

## ▶️ Rulare

Cerințe: **JDK 25** (proiectul este configurat pentru Java 25; fără el, build-ul eșuează) și
opțional o cheie **Gemini API**. Maven nu trebuie instalat - se folosește wrapper-ul `./mvnw`.

```bash
export GEMINI_API_KEY=cheia_ta     # obligatoriu pentru partea de AI
./mvnw spring-boot:run
```

Serverul pornește pe `http://localhost:8080`; baza `vacation_quester.db` și tabelele se creează automat (baza pornește goală).

Pentru verificarea rapidă a proiectului:

```bash
./mvnw test
```

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
- CORS este deschis pentru orice origine (`WebConfig`), ca frontend-ul Vue să poată apela serverul.
- Erori: `400` pentru date invalide, `404` pentru resurse inexistente, `409` pentru o sarcină
  inactivă, `503` când lipsește cheia Gemini. Mesajul apare în corpul răspunsului
  (`spring.web.error.include-message=always`).
- Ce este implementat și ce nu: vezi [Project_requirements.md](Project_requirements.md).
