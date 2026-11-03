# Verification report

## Verified (reproducible: `bash verification/run_checks.sh`, needs JDK 21 + python3 only)
| Check | Result |
|---|---|
| All entity, repository, service, mapper, DTO and controller code type-checked with the real JDK 21 compiler (Spring/JPA/Lombok replaced by thin stubs) | **0 errors** |
| Grading logic executed on the compiled classes: MCQ, multi-select, true/false, short-answer, descriptive, negative marking, grade bands, evaluator registry | **26/26 pass** |
| `database/schema.sql` vs JPA entities: every table, column, unique key and index column (19 tables) | **match** |
| All 25 `@Query` JPQL statements and every derived query method resolved against entity properties | **no unknown property** |
| Negative control (typos injected into SQL and JPQL) | detected, so the checks are real |
| Spring bean dependency graph (36 beans) | **no circular dependencies** |
| Tables created in FK-dependency order in schema.sql | **yes** |

## NOT verified in the build environment (no Maven, no network, no MySQL server there)
* `mvn` dependency resolution and a real Spring context start-up.
* Executing `schema.sql` on MySQL 8 and the generated SQL at runtime.
* `SecurityConfig`, `JwtService`, `JwtAuthFilter`, `GlobalExceptionHandler` (excluded from the stub compile; they use standard Spring Security 6 / jjwt 0.12 APIs).
* Concurrency behaviour (row locks) and the scheduler under load.

## Confirm on your machine (10 minutes)
1. `mvn test` -> JUnit tests pass; `mvn spring-boot:run` -> log ends with `Started ParikshaGuruApplication` and `Seeded administrator`.
2. MySQL Workbench -> run `SHOW TABLES FROM parikshaguru_db;` -> expect **19 rows**; `SELECT email, role, status FROM users;` -> 4 users with `password_hash` starting `$2a$`.
3. `python3 verification/smoke_test.py` -> runs ~50 live assertions covering login, subject/exam/question creation, publish, attend, autosave, submit, duplicate-submit block, auto + manual grading, approve, publish, student result view, logout.
4. Open `http://localhost:8080/swagger-ui.html`.

If any step fails, send me the console output or the failing line and I will fix it.
