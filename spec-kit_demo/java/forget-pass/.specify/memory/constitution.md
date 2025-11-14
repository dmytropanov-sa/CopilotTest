# Forget-Pass Application Constitution

## Core Principles

### I. Twelve-Factor Application Compliance (NON-NEGOTIABLE)
All components must strictly adhere to the [12-Factor App methodology](https://12factor.net/):
- **Codebase**: Single codebase tracked in Git, multiple deployments (dev, staging, prod)
- **Dependencies**: Explicit declaration via Maven/Gradle (backend) and npm/package.json (frontend); No system-wide packages
- **Config**: Store configuration in environment variables; Never commit secrets to version control
- **Backing Services**: Treat databases, caches, message queues as attached resources accessible via URLs
- **Build, Release, Run**: Strict separation of build and run stages; Immutable releases
- **Processes**: Execute as stateless processes; Any persistent state stored in backing services
- **Port Binding**: Services export via port binding; Backend exposes HTTP service; No web server dependencies in code
- **Concurrency**: Scale out via horizontal process model; Design for stateless horizontal scaling
- **Disposability**: Fast startup (<30s) and graceful shutdown; Handle SIGTERM signals properly
- **Dev/Prod Parity**: Keep development, staging, and production as similar as possible; Same backing services
- **Logs**: Treat logs as event streams; Write to stdout/stderr; Use centralized logging (ELK, Splunk)
- **Admin Processes**: Run admin/management tasks as one-off processes; Use same codebase and environment

### II. Clean Code & SOLID Principles (NON-NEGOTIABLE)
All code must follow clean code principles and SOLID design:
- **Meaningful Names**: Classes, methods, variables must have intention-revealing names
- **Single Responsibility**: Each class/method does one thing and does it well
- **Small Functions**: Methods should be <20 lines; Do one thing at one level of abstraction
- **DRY Principle**: Don't Repeat Yourself - extract common logic into reusable components
- **Comments**: Code should be self-documenting; Comments explain "why" not "what"
- **Error Handling**: Proper exception handling; Use custom exceptions; Never swallow exceptions
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Package Structure**: Domain-driven package organization (by feature, not by layer)

### III. Comprehensive Testing Strategy (NON-NEGOTIABLE - 80%+ Coverage)
Multi-layered testing approach with strict coverage thresholds:

**Backend Testing (Spring Boot)**:
- **Unit Tests** (JUnit 5, Mockito): Test business logic in isolation; Mock all dependencies; 90%+ coverage
- **Integration Tests** (Spring Boot Test, TestContainers): Test API endpoints, database interactions, service layers; Real database via TestContainers
- **Contract Tests** (Spring Cloud Contract): API contract verification between services
- **Security Tests**: Authentication/authorization flows, input validation, SQL injection prevention
- **Acceptance Tests** (Cucumber, REST Assured): BDD scenarios in Gherkin; End-to-end API validation

**Frontend Testing (Angular)**:
- **Unit Tests** (Jasmine, Karma): Test components, services, pipes in isolation; 90%+ coverage
- **Integration Tests**: Test component interactions with services; Mock HTTP calls
- **E2E Tests** (Cypress, Playwright): Full user workflows; Cross-browser testing; 80%+ critical path coverage
- **Accessibility Tests**: WCAG 2.1 AA compliance; Automated a11y checks

**Quality Gates**:
- Minimum 80% overall code coverage (enforced by SonarQube/Jacoco)
- All tests must pass before merge
- No critical/blocker SonarQube violations
- Performance budgets: API response <200ms (p95), Frontend FCP <1.5s

### IV. Minimal Dependencies & Dependency Injection
Optimize dependency management to reduce complexity and security vulnerabilities:

**Backend (Spring Boot)**:
- Use Spring Boot Starter dependencies for common functionality
- Leverage Constructor Injection exclusively (no field injection)
- Use `@RequiredArgsConstructor` from Lombok for cleaner constructor injection
- Avoid unnecessary dependencies; Justify each dependency in documentation
- Regular dependency audits using OWASP Dependency-Check
- Use Spring's `@Conditional` annotations for optional features
- Prefer interfaces over concrete classes for better testability

**Frontend (Angular)**:
- Minimize npm package count; Use Angular built-in features first
- Implement dependency injection via Angular's DI container
- Use `providedIn: 'root'` for singleton services
- Lazy load feature modules to reduce initial bundle size
- Tree-shaking optimization; Analyze bundle with webpack-bundle-analyzer
- Regular `npm audit` and dependency updates

**Dependency Principles**:
- Keep dependency tree shallow
- Avoid transitive dependency conflicts
- Document architectural decisions (ADRs) for major dependencies
- Use BOM (Bill of Materials) for version management

### V. Security First (Priority Quality Attribute)
Security is the highest priority quality attribute and must be embedded in every layer:

**Backend Security**:
- Spring Security with JWT-based authentication
- Password encryption with BCrypt (strength ≥12)
- HTTPS/TLS 1.3 only; HSTS headers enabled
- OWASP Top 10 mitigation: SQL injection prevention (prepared statements), XSS protection, CSRF tokens
- Input validation using Bean Validation (JSR-380)
- Rate limiting and throttling on sensitive endpoints
- Secrets management via HashiCorp Vault or AWS Secrets Manager
- Security headers: Content-Security-Policy, X-Frame-Options, X-Content-Type-Options
- Regular penetration testing and security audits
- Audit logging for all authentication and authorization events

**Frontend Security**:
- Content Security Policy implementation
- XSS prevention via Angular's built-in sanitization
- Secure storage: Never store sensitive data in localStorage
- HTTPS-only; Secure cookies with HttpOnly and SameSite flags
- Dependency scanning for known vulnerabilities (npm audit, Snyk)
- Regular OWASP ZAP or Burp Suite scanning

**DevSecOps**:
- Static Application Security Testing (SAST) in CI/CD pipeline
- Dynamic Application Security Testing (DAST) in staging environment
- Container image scanning (Trivy, Clair)
- Secret scanning in Git commits (git-secrets, TruffleHog)

## Technical Architecture Standards

### Backend: Spring Boot Best Practices
Follow Spring community conventions and modern best practices:

**Project Structure**:
```
src/main/java/com/app/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic
├── repository/       # Data access layer
├── domain/           # Entities and domain models
├── dto/              # Data Transfer Objects
├── mapper/           # DTO-Entity mappers (MapStruct)
├── exception/        # Custom exceptions and handlers
├── security/         # Security configuration
└── util/             # Utility classes
```

**Spring Boot Standards**:
- Use Spring Boot 3.x with Java 17+ LTS
- RESTful API design following Richardson Maturity Model Level 2-3
- OpenAPI 3.0 documentation with Springdoc-openapi
- Use `@RestController`, `@Service`, `@Repository` stereotypes appropriately
- Implement `@ControllerAdvice` for global exception handling
- Use Spring Data JPA with repository interfaces
- Implement HATEOAS for API discoverability where appropriate
- Pagination and sorting for list endpoints (Spring Data Pageable)
- API versioning via URI path (/api/v1/)
- Use `@Transactional` with proper propagation and isolation levels
- Implement caching with Spring Cache abstraction (@Cacheable)
- Use Spring Profiles for environment-specific configuration

**Database & Persistence**:
- Flyway or Liquibase for database migrations
- Use JPA entities with proper relationships and fetch strategies
- Avoid N+1 query problems (use JOIN FETCH, EntityGraph)
- Database connection pooling (HikariCP)
- Read-only transactions for queries
- Optimistic locking for concurrent updates

### Frontend: Angular Best Practices
Follow Angular style guide and modern reactive patterns:

**Project Structure**:
```
src/app/
├── core/             # Singleton services, guards, interceptors
├── shared/           # Shared components, directives, pipes
├── features/         # Feature modules (lazy-loaded)
│   ├── auth/
│   ├── dashboard/
│   └── user-profile/
├── models/           # TypeScript interfaces and types
├── services/         # API services
└── interceptors/     # HTTP interceptors
```

**Angular Standards**:
- Use Angular 17+ with standalone components where appropriate
- Reactive forms over template-driven forms
- RxJS for reactive programming; Use operators correctly (switchMap, debounceTime, etc.)
- Smart/Container vs Dumb/Presentational component pattern
- OnPush change detection strategy for performance
- Lazy loading for feature modules
- Route guards for authentication and authorization
- HTTP interceptors for token injection and error handling
- State management with NgRx or Signals for complex applications
- Use Angular CLI for scaffolding and builds
- TypeScript strict mode enabled
- Accessibility: ARIA labels, keyboard navigation, screen reader support

**Responsive Design & UX**:
- Mobile-first approach using CSS Grid and Flexbox
- Angular Material or PrimeNG component library
- Breakpoints: Mobile (<768px), Tablet (768-1024px), Desktop (>1024px)
- Touch-friendly targets (minimum 44x44px)
- Progressive enhancement; Graceful degradation
- Loading indicators for async operations
- Error messages that guide users to resolution
- Consistent visual design system (colors, typography, spacing)
- Accessibility: WCAG 2.1 AA compliance

### Interoperability Standards
Ensure seamless integration and communication:

**API Design**:
- RESTful APIs following OpenAPI 3.0 specification
- JSON as primary data exchange format
- Consistent error response format (RFC 7807 Problem Details)
- Content negotiation support (Accept headers)
- CORS configuration for cross-origin requests
- API rate limiting and throttling
- Webhook support for event-driven integrations
- GraphQL endpoints for complex data fetching (optional)

**Integration Patterns**:
- Circuit breaker pattern for external service calls (Resilience4j)
- Retry mechanisms with exponential backoff
- Idempotent operations for safe retries
- Event-driven architecture with message brokers (RabbitMQ, Kafka) where appropriate
- API Gateway pattern for microservices orchestration

### Maintainability Standards
Code must be maintainable, readable, and evolvable:

**Code Quality**:
- SonarQube analysis in CI/CD pipeline
- Code review mandatory for all changes (2+ approvers)
- Automated code formatting (Prettier, Checkstyle, Spotless)
- Linting enforced (ESLint for Angular, Checkstyle for Java)
- Technical debt tracked and scheduled for resolution
- Refactoring sprints allocated quarterly

**Documentation**:
- Inline Javadoc/JSDoc for public APIs
- README files for each module
- Architecture Decision Records (ADRs) for significant decisions
- API documentation auto-generated from code (Springdoc, Compodoc)
- Living documentation via tests (BDD scenarios)
- Onboarding guide for new developers
- Runbook for operations team

**Version Control**:
- Git with feature branch workflow
- Conventional Commits specification (feat:, fix:, docs:, etc.)
- Semantic versioning (MAJOR.MINOR.PATCH)
- Pull request templates with checklist
- Branch protection rules on main branch
- Git hooks for pre-commit validation (Husky)

## Development Workflow & Quality Gates

### CI/CD Pipeline
Automated pipeline ensuring quality at every stage:

**Build Stage**:
1. Compile code (Maven/Gradle for backend, npm build for frontend)
2. Run linters and formatters
3. Static code analysis (SonarQube)
4. Dependency vulnerability scanning

**Test Stage**:
1. Unit tests with coverage reports
2. Integration tests
3. Contract tests
4. Security tests (SAST)
5. Fail build if coverage <80%

**Release Stage**:
1. Build Docker images
2. Scan container images for vulnerabilities
3. Push to container registry
4. Deploy to staging environment
5. Run E2E tests and smoke tests
6. DAST security scanning
7. Performance testing (JMeter, k6)
8. Manual approval for production
9. Blue-green or canary deployment to production
10. Automated rollback on failure

### Monitoring & Observability
Comprehensive monitoring for operational excellence:

**Logging**:
- Structured JSON logging
- Correlation IDs for distributed tracing
- Log levels: ERROR, WARN, INFO, DEBUG
- Centralized logging (ELK Stack, Splunk)
- Log retention policy (30 days minimum)

**Metrics**:
- Application metrics (Spring Boot Actuator, Micrometer)
- Infrastructure metrics (CPU, memory, disk, network)
- Business metrics (user registrations, transactions)
- Prometheus for metrics collection
- Grafana dashboards for visualization

**Tracing**:
- Distributed tracing with Zipkin or Jaeger
- OpenTelemetry instrumentation
- Trace all external service calls

**Alerting**:
- Alert on critical errors and performance degradation
- PagerDuty or similar for incident management
- Runbooks linked to alerts

## Governance

### Constitutional Authority
This constitution is the supreme authority governing all development practices for the Forget-Pass application. It supersedes all other development guidelines, coding standards, and practices.

### Compliance Requirements
- All pull requests must demonstrate compliance with constitutional principles
- Code reviews must verify adherence to security, testing, and quality standards
- Non-compliance blocks merge; Technical debt requires explicit approval and remediation plan
- Quarterly audits of codebase against constitutional standards

### Amendment Process
1. Propose amendment with rationale and impact analysis
2. Review by architecture committee
3. Approval requires consensus from technical leads
4. Document amendment with date and version bump
5. Communicate changes to all team members
6. Update related documentation and tooling

### Quality Attribute Priorities
1. **Security**: Non-negotiable; highest priority
2. **Interoperability**: APIs and integrations must be robust
3. **Usability**: Intuitive UX and accessibility
4. **Maintainability**: Code must be sustainable long-term

**Version**: 1.0.0 | **Ratified**: 2025-11-14 | **Last Amended**: 2025-11-14
