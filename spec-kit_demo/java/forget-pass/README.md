# Forget Pass — Patient Registration & Password Recovery

Multi-module project:

- `backend`: Spring Boot (Jetty) + SQLite
- `frontend`: Angular + Tailwind (planned)

## Build & Run (Backend)

```cmd
cd backend
mvn clean spring-boot:run
```

Backend serves `http://localhost:8080/api/v1/health`.

## Configuration

- SQLite DB file: `${user.dir}/data/forget-pass.db`
- BCrypt strength: `app.security.bcrypt-strength` (default 12)

## Testing & Coverage

```cmd
cd backend
mvn clean test
```

Generates JaCoCo report at `backend/target/site/jacoco/index.html`.

## Next Steps

- Scaffold Angular app with Tailwind theme
- Implement registration, verification, password reset flows
- Add rate limiting, reCAPTCHA, audit logging, and security headers
