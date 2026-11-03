# ParikshaGuru Backend (Java 21 · Spring Boot 3 · MySQL 8)

REST API for the ParikshaGuru Central Examination & Proctoring Portal.
Layers: `entity → repository → service (interface + impl) → controller`, DTOs (records), `mapper`, `security` (JWT), `exception`, `config`, `scheduler`.
OOP highlights: `BaseEntity` inheritance, `User`/`Student`/`Faculty`, service interfaces + DI, `QuestionEvaluator` strategy
(polymorphism: `SingleChoiceEvaluator`, `MultiSelectEvaluator`, `ShortAnswerEvaluator`, `DescriptiveEvaluator`), `GradingPolicy` interface.

## 1. Prerequisites
JDK 21, Maven 3.9+, MySQL 8 + MySQL Workbench.

## 2. Database (MySQL Workbench)
1. Open Workbench → `+` next to *MySQL Connections* → Hostname `127.0.0.1`, Port `3306`, Username `root` → *Test Connection* → OK → open it.
2. **Option A (recommended):** `File → Open SQL Script → database/schema.sql` → click the ⚡ Execute button. Creates `parikshaguru_db` and all 19 tables.
   **Option B:** skip it — the app creates the database and tables on first start (`ddl-auto=update`).
3. Refresh *Schemas* (circular arrows) to see `parikshaguru_db`.

## 3. Run
```bash
export DB_USERNAME=root DB_PASSWORD=your_mysql_password      # Windows PowerShell: $env:DB_PASSWORD="..."
export JWT_SECRET="at-least-32-characters-long-random-secret"
mvn spring-boot:run
```
API: `http://localhost:8080/api` · Swagger UI: `http://localhost:8080/swagger-ui.html` (use **Authorize** and paste the JWT).
Tests: `mvn test`.

On first start the app seeds departments, an admin and demo data (set `SEED_DEMO_DATA=false` for production):

| Role | Email | Password |
|---|---|---|
| Admin | admin@university.ac.in | Admin@12345 |
| Faculty | nair@university.ac.in | Faculty@123 |
| Student | rohan.verma@university.ac.in | Student@123 |
| Student | aarav.sharma@university.ac.in | Student@123 |

Demo exam `EX-CS602-DEMO` (5 questions, 10 marks) is already published and assigned to both students.
Optionally run `database/sample_data.sql` afterwards for extra subjects.

## 4. Configuration (environment variables)
`DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION_MINUTES, CORS_ORIGINS` (add your Lovable URL),
`ALLOWED_EMAIL_DOMAINS` (e.g. `university.ac.in`), `FACULTY_INVITE_CODE`, `STUDENT_AUTO_ACTIVATE`, `EXPOSE_RESET_TOKEN` (**false in production**), `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

## 5. Behaviour summary
* **Registration:** students become ACTIVE (or PENDING if `STUDENT_AUTO_ACTIVATE=false`); faculty with a valid invite code become ACTIVE, otherwise PENDING until an admin calls `PATCH /api/admin/users/{id}/status`; ADMIN self-registration is rejected (403). Logout/password change invalidate older JWTs.
* **Exam engine:** `POST /exams/{id}/start` creates one session per student+exam (DB unique key + row lock), deadline = min(start+duration, exam end). The server clock decides expiry; a scheduler auto-submits expired sessions even if the browser is closed. Answers autosave per question; refresh/resume returns saved answers. Submission, auto-grading and result creation happen in one transaction. Duplicate submit → 409. Students never receive correct answers.
* **Grading:** MCQ/multi-select/true-false/short-answer are auto-graded (negative marking when enabled); descriptive answers need `POST /submissions/{id}/evaluate`.
* **Results:** `PENDING_APPROVAL → APPROVED → PUBLISHED`. Edits after approval revoke approval; after publication they are rejected (409). History: `GET /results/{id}/history`. Students see marks only when PUBLISHED.
* **Proctoring:** tab visibility, fullscreen exit, connection loss/restore and exam start/end are logged with server timestamps. These are *indicators for review only* — browser events are not reliable identity verification or complete cheating prevention.
* **Password reset:** no SMTP is configured. With `EXPOSE_RESET_TOKEN=true` the token is returned by the API (dev only); in production add a mail sender in `AuthServiceImpl.forgotPassword`.

## 6. API list
Auth `/api/auth/{register,login,logout,forgot-password,reset-password,me,change-password}` · Student `/api/students/{dashboard,profile,exams,results}` ·
Admin `/api/admin/{dashboard,users}`, `/api/departments` · Subjects `/api/subjects` · Exams `/api/exams` (+`/publish`, `/assign`, `/questions`) · `/api/questions/{id}` ·
Sessions `/api/exams/{id}/start`, `/api/sessions/{id}[/answers|/mark-review|/submit|/proctoring-events]` ·
Submissions `/api/submissions[/{id}|/{id}/evaluate]` · Results `/api/results[/{id}|/{id}/approve|/{id}/publish|/bulk-approve|/bulk-publish|/{id}/history]` · Notifications `/api/notifications`.
Import `postman/ParikshaGuru.postman_collection.json` (run the three login requests first; the last login sets `{{token}}`, so re-run the login of the role you want to test).

## 7. Verification
See `VERIFICATION.md`. Offline: `bash verification/run_checks.sh`. Live (server + MySQL running): `python3 verification/smoke_test.py`.

## 8. Frontend
See `frontend-integration/INTEGRATION.md` (API layer, auth context, protected routes, exam hook).
