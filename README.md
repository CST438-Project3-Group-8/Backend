# StudyHive — Spring Boot Backend

A RESTful API backend for the StudyHive platform, built with Spring Boot 4, Spring Security (JWT / OAuth2 Resource Server), Spring Data JPA, and PostgreSQL (hosted on Supabase).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.5 |
| Language | Java 21 |
| Security | Spring Security · OAuth2 Resource Server · JWT (Supabase-issued tokens) |
| Persistence | Spring Data JPA · Hibernate · PostgreSQL |
| Connection Pool | HikariCP (tuned for Supabase session pooler) |
| Build | Maven 3.9 (via Maven Wrapper) |
| Containerisation | Docker · Docker Compose |

---

## Project Structure

```
src/main/java/com/studyhive/spring_boot_docker/
├── SecurityConfig.java          # CORS, JWT resource server, route auth rules
├── SpringBootDockerApplication.java
│
├── Course.java / CourseController.java / CourseRepository.java
├── CourseDataLoader.java        # Seeds CSUMB CST course catalogue on startup
│
├── StudyGroup.java / GroupController.java / StudyGroupRepository.java
├── GroupMember.java / GroupMemberRepository.java
│
├── StudySession.java / StudySessionController.java / StudySessionRepository.java
├── StudySessionRequest.java / StudySessionResponse.java
│
├── User.java / UserController.java / UserRepository.java
├── UserCourse.java / UserCourseRepository.java
├── UpdateProfileRequest.java
└── OauthProvider.java           # EMAIL | GOOGLE | GITHUB enum
```

---

## API Endpoints

### Courses `GET /api/courses`
Returns all seeded CSUMB CST courses. Public (no auth required).

### Groups `/api/groups`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/groups` | Public | List all groups |
| GET | `/api/groups/{id}` | Public | Get group by ID |
| POST | `/api/groups` | JWT | Create a group |
| DELETE | `/api/groups/{id}` | JWT (owner) | Delete a group |
| POST | `/api/groups/{id}/join` | JWT | Join a group |
| DELETE | `/api/groups/{id}/leave` | JWT | Leave a group |
| GET | `/api/groups/{id}/membership` | Public* | Check membership |
| GET | `/api/groups/{id}/members` | Public | List members |
| GET | `/api/groups/me/joined` | JWT | My joined groups |

### Sessions `/api/sessions`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/sessions/group/{groupId}` | Public | Sessions for a group |
| GET | `/api/sessions/{id}` | Public | Single session |
| POST | `/api/sessions` | JWT (group owner) | Create session |
| PUT | `/api/sessions/{id}` | JWT (group owner) | Update session |
| DELETE | `/api/sessions/{id}` | JWT (group owner) | Delete session |

### Users `/api/user`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/user` | JWT | Bootstrap / upsert profile |
| GET | `/api/user/me` | JWT | Get my profile |
| PUT | `/api/user` | JWT | Update name / bio / major |
| DELETE | `/api/user/me` | JWT | Delete account |
| GET | `/api/user/me/courses` | JWT | My enrolled courses |
| POST | `/api/user/me/courses/{courseId}` | JWT | Enrol in a course |
| DELETE | `/api/user/me/courses/{courseId}` | JWT | Drop a course |

---

## Authentication

All protected endpoints expect a Supabase-issued JWT in the `Authorization: Bearer <token>` header. The `issuer-uri` is configured in `application.properties` and Spring Security validates tokens automatically.

The `creatorId` / user identity is read from `jwt.getSubject()` (the Supabase user UUID).

---

## Running Locally

### Prerequisites
- Java 21
- Maven 3.9+ **or** use the included `./mvnw` wrapper
- Docker & Docker Compose (for containerised runs)
- A Supabase project (PostgreSQL + Auth)

### Environment Variables

Create a `.env` file in the project root:

```env
DB_PASSWORD=your_supabase_db_password
```

The `application.properties` file already contains the Supabase pooler URL and JWT issuer URI — swap these for your own project values if you fork the repo.

### Option A — Docker Compose (recommended)

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

### Option B — Maven

```bash
./mvnw spring-boot:run
```

### Option C — Build JAR then run

```bash
./mvnw -DskipTests package
java -jar target/*.jar
```

---

## Running Tests

```bash
./mvnw test
```

Unit tests live in `src/test/` and use Mockito to mock repositories — no database required.

---

## Database

The app targets Supabase PostgreSQL via the **session pooler** (`port 6543`, `pgbouncer=true&prepareThreshold=0`) to avoid IPv6 resolution issues in Docker. `spring.jpa.hibernate.ddl-auto=update` keeps the schema in sync automatically.

HikariCP is tuned for the free-tier Supabase connection limit:

```properties
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=0
```

---

## CORS

Allowed origins are configured in `SecurityConfig`:

```
http://localhost:5173   ← Vite dev server (web frontend)
http://localhost:8080   ← Same-origin
```

Add your production frontend URL to this list before deploying.

---

## Docker

The multi-stage `Dockerfile` uses `maven:3.9.11-eclipse-temurin-21` to build and `eclipse-temurin:21-jre` as the runtime image. Dependencies are cached in a separate layer so incremental rebuilds are fast.

```dockerfile
FROM maven:3.9.11-eclipse-temurin-21 AS builder
...
FROM eclipse-temurin:21-jre
```
