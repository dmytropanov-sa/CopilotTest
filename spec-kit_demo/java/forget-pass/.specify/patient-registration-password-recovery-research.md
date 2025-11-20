# Implementation Plan: Patient Registration & Password Recovery

**Branch**: `001-patient-registration-password-recovery` | **Date**: 2025-11-14 | **Spec**: patient-registration-password-recovery-spec.md
**Input**: Feature specification for patient registration and password recovery system

## Summary

Implement a secure patient registration and password recovery system for a healthcare platform. The system will allow new patients (18+) to register with email verification, enforce strong password policies, and provide secure password reset functionality. Backend will use Spring Boot with SQLite database and embedded Tomcat web server, frontend will use Angular with Tailwind CSS custom theme. Full test coverage (85%+) will be maintained across all layers including unit, integration, E2E, and security tests.

## Technical Context

**Language/Version**: Java 17 LTS (backend), TypeScript 5.2+ (frontend)  
**Primary Dependencies**: Spring Boot 3.2+ (backend), Angular 17+ (frontend), SQLite 3.44+ (database)  
**Storage**: SQLite database with file-based storage (no external DB server required)  
**Testing**: JUnit 5, Mockito, Spring Boot Test, H2 (backend); Jasmine, Karma, Cypress (frontend)  
**Target Platform**: Local development with embedded web server, production-ready standalone JAR  
**Project Type**: Full-stack web application (Spring Boot backend + Angular frontend)  
**Performance Goals**: API response <500ms (p95), email delivery <60s, support 100+ concurrent users  
**Constraints**: Standalone deployment (no external dependencies), HIPAA compliance, WCAG 2.1 AA accessibility  
**Scale/Scope**: 500 concurrent registrations peak, 85%+ test coverage, 8-week timeline  

## Constitution Check

*GATE: Must pass before Phase 0 research. All requirements aligned with constitution principles.*

✅ **Twelve-Factor App Compliance**: Standalone JAR deployment, environment-based configuration, stateless processes  
✅ **Clean Code & SOLID**: Domain-driven design, dependency injection, clear separation of concerns  
✅ **Comprehensive Testing**: 85%+ coverage with unit, integration, E2E, and security tests  
✅ **Minimal Dependencies**: Core Spring Boot starters, essential libraries only  
✅ **Security First**: BCrypt password hashing, rate limiting, audit logging, HIPAA compliance  
✅ **Spring Boot Best Practices**: RESTful APIs, JPA entities, proper validation  
✅ **Angular Best Practices**: Reactive forms, lazy loading, accessibility compliance  
✅ **Responsive Design**: Mobile-first with Tailwind CSS, WCAG 2.1 AA compliance  
✅ **Quality Attributes**: Security (highest priority), interoperability, usability, maintainability  

## Project Structure

### Documentation (this feature)

```text
.specify/
├── patient-registration-password-recovery-spec.md    # Feature specification
├── patient-registration-password-recovery-plan.md     # This implementation plan
├── patient-registration-password-recovery-research.md # Phase 0 research output
├── patient-registration-password-recovery-data-model.md # Phase 1 data model
├── patient-registration-password-recovery-quickstart.md # Phase 1 quickstart guide
├── contracts/                                        # Phase 1 API contracts
└── patient-registration-password-recovery-tasks.md   # Phase 2 task breakdown
```

### Source Code (repository root)

```text
patient-auth-system/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/healthcare/patientauth/
│   │   ├── config/                   # Configuration classes
│   │   │   ├── DatabaseConfig.java   # SQLite configuration
│   │   │   ├── SecurityConfig.java   # Spring Security setup
│   │   │   ├── EmailConfig.java      # SendGrid configuration
│   │   │   └── WebConfig.java        # CORS, CSRF settings
│   │   ├── controller/               # REST controllers
│   │   │   ├── PatientRegistrationController.java
│   │   │   ├── PasswordResetController.java
│   │   │   └── EmailVerificationController.java
│   │   ├── service/                  # Business logic services
│   │   │   ├── PatientRegistrationService.java
│   │   │   ├── PasswordResetService.java
│   │   │   ├── EmailVerificationService.java
│   │   │   ├── PasswordValidationService.java
│   │   │   ├── EmailValidationService.java
│   │   │   ├── EmailService.java
│   │   │   └── RateLimitingService.java
│   │   ├── repository/               # Data access layer
│   │   │   ├── PatientRepository.java
│   │   │   ├── PatientCredentialRepository.java
│   │   │   ├── EmailVerificationTokenRepository.java
│   │   │   ├── PasswordResetTokenRepository.java
│   │   │   └── AuthenticationAuditLogRepository.java
│   │   ├── model/                    # JPA entities
│   │   │   ├── Patient.java
│   │   │   ├── PatientCredential.java
│   │   │   ├── EmailVerificationToken.java
│   │   │   ├── PasswordResetToken.java
│   │   │   └── AuthenticationAuditLog.java
│   │   ├── dto/                      # Data transfer objects
│   │   │   ├── PatientRegistrationRequest.java
│   │   │   ├── PasswordResetRequest.java
│   │   │   └── ValidationResponse.java
│   │   ├── exception/                # Custom exceptions
│   │   │   ├── PatientRegistrationException.java
│   │   │   └── SecurityException.java
│   │   ├── security/                 # Security components
│   │   │   ├── PasswordEncoderConfig.java
│   │   │   ├── RateLimitingFilter.java
│   │   │   ├── RecaptchaV3Validator.java
│   │   │   └── SecurityHeadersFilter.java
│   │   └── util/                     # Utility classes
│   │       ├── TokenGenerator.java
│   │       └── DisposableEmailChecker.java
│   ├── src/main/resources/
│   │   ├── application.properties    # Configuration
│   │   ├── db/migration/             # Flyway migrations
│   │   │   └── V1__Initial_schema.sql
│   │   └── static/                   # Static resources (if needed)
│   └── src/test/java/com/healthcare/patientauth/
│       ├── unit/                     # Unit tests
│       ├── integration/              # Integration tests
│       ├── security/                 # Security tests
│       └── performance/              # Performance tests
├── frontend/                         # Angular application
│   ├── src/app/
│   │   ├── core/                     # Core services and guards
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── validation.service.ts
│   │   │   │   └── error-handler.service.ts
│   │   │   ├── guards/
│   │   │   │   └── anonymous.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   ├── csrf.interceptor.ts
│   │   │   │   └── error.interceptor.ts
│   │   │   └── config/
│   │   │       └── api.config.ts
│   │   ├── features/auth/            # Authentication feature module
│   │   │   ├── components/
│   │   │   │   ├── registration/
│   │   │   │   │   ├── registration.component.ts
│   │   │   │   │   ├── registration.component.html
│   │   │   │   │   └── registration.component.scss
│   │   │   │   ├── password-reset-request/
│   │   │   │   ├── password-reset/
│   │   │   │   ├── email-verification/
│   │   │   │   └── password-strength-indicator/
│   │   │   ├── services/
│   │   │   │   ├── auth-api.service.ts
│   │   │   │   └── form-validation.service.ts
│   │   │   └── auth.module.ts
│   │   ├── shared/                   # Shared components and utilities
│   │   │   ├── components/
│   │   │   │   ├── error-message/
│   │   │   │   └── loading-spinner/
│   │   │   ├── directives/
│   │   │   │   └── form-validation.directive.ts
│   │   │   └── pipes/
│   │   │       └── phone-format.pipe.ts
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.scss
│   │   └── app.module.ts
│   ├── src/assets/
│   │   └── styles/
│   │       ├── theme.scss             # Tailwind theme configuration
│   │       └── global.scss
│   ├── src/environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   └── src/test.ts
├── docker/                           # Docker configuration
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── docker-compose.yml
├── docs/                             # Documentation
│   ├── api/
│   ├── deployment/
│   └── user-guide/
├── scripts/                          # Build and deployment scripts
│   ├── build.sh
│   ├── test.sh
│   └── deploy.sh
├── pom.xml                           # Maven configuration
├── package.json                      # Frontend dependencies
├── tailwind.config.js                # Tailwind CSS configuration
├── angular.json                      # Angular CLI configuration
├── README.md
└── .gitignore
```

**Structure Decision**: Full-stack web application with clear separation between backend (Spring Boot) and frontend (Angular). Backend uses Maven for build management, SQLite for file-based storage, and embedded Tomcat for standalone deployment. Frontend uses Angular CLI with Tailwind CSS for styling. Test directories mirror source structure for maintainability.

## Implementation Phases

### Phase 0: Research & Setup (Week 1, Days 1-2)

**Objectives**: Validate technology choices, set up development environment, create project skeleton

**Tasks**:
1. **Technology Validation**
   - Verify Spring Boot 3.2+ compatibility with SQLite and embedded Tomcat
   - Test Angular 17+ with Tailwind CSS @theme configuration
   - Validate SendGrid integration for HIPAA compliance
   - Confirm Google reCAPTCHA v3 integration with Spring Boot

2. **Development Environment Setup**
   - Install Java 17, Maven 3.9+, Node.js 18+, SQLite 3.44+
   - Configure IDE (IntelliJ IDEA for backend, VS Code for frontend)
   - Set up Git repository with feature branch

3. **Project Skeleton Creation**
   - Generate Spring Boot project with Maven
   - Create Angular project with CLI
   - Configure Tailwind CSS with custom healthcare theme
   - Set up SQLite database configuration
   - Initialize Git repository structure

**Deliverables**:
- Functional development environment
- Basic project structure with build scripts
- Tailwind CSS theme configuration
- SQLite database setup
- Initial CI/CD pipeline configuration

### Phase 1: Core Backend Implementation (Week 1-2, Days 3-7)

**Objectives**: Implement patient registration and password reset backend functionality

**Tasks**:
1. **Database Layer**
   - Create JPA entities for Patient, PatientCredential, EmailVerificationToken, PasswordResetToken, AuthenticationAuditLog
   - Configure SQLite dialect and connection
   - Implement Flyway migrations for schema setup
   - Create repository interfaces with custom queries

2. **Security Implementation**
   - Configure Spring Security with BCrypt password encoder
   - Implement rate limiting with Caffeine cache
   - Add Google reCAPTCHA v3 validation
   - Configure security headers (CSP, HSTS, X-Frame-Options)

3. **Business Logic Services**
   - PatientRegistrationService with email validation and disposable email blocking
   - PasswordResetService with secure token generation
   - EmailVerificationService with resend limits
   - PasswordValidationService with strength requirements

4. **API Controllers**
   - PatientRegistrationController with validation endpoints
   - PasswordResetController with token management
   - EmailVerificationController with verification logic

5. **Email Integration**
   - SendGrid service integration
   - Email templates for verification and password reset
   - Error handling for email delivery failures

**Deliverables**:
- Complete backend API with all endpoints
- SQLite database with proper schema
- Email sending functionality
- Security configurations
- Unit tests for all services (80%+ coverage)

### Phase 2: Frontend Implementation (Week 3-4, Days 8-14)

**Objectives**: Build responsive Angular frontend with healthcare theme

**Tasks**:
1. **Tailwind CSS Theme Configuration**
   - Define healthcare color palette (@theme colors)
   - Configure responsive breakpoints
   - Set up typography and spacing scales
   - Create component-specific utility classes

2. **Authentication Components**
   - RegistrationComponent with reactive forms
   - PasswordResetRequestComponent
   - PasswordResetComponent with strength indicator
   - EmailVerificationComponent

3. **Form Validation & UX**
   - Custom validators for email, password, phone
   - Real-time validation feedback
   - Accessible error messages
   - Loading states and progress indicators

4. **API Integration**
   - AuthenticationService for API calls
   - HTTP interceptors for CSRF and error handling
   - Form validation service
   - Anonymous guard for route protection

5. **Responsive Design**
   - Mobile-first approach with Tailwind utilities
   - Accessible navigation and forms
   - Cross-browser compatibility testing

**Deliverables**:
- Complete frontend application
- Tailwind CSS custom theme
- Responsive design across all screen sizes
- Accessibility compliance (WCAG 2.1 AA)
- Unit tests for components and services

### Phase 3: Integration & Testing (Week 5-6, Days 15-21)

**Objectives**: Integrate frontend/backend, implement comprehensive testing

**Tasks**:
1. **System Integration**
   - Configure CORS for local development
   - Test end-to-end registration flow
   - Validate password reset workflow
   - Verify email verification process

2. **Backend Testing**
   - Unit tests for all services and controllers (90%+ coverage)
   - Integration tests with H2 database
   - Security tests for authentication flows
   - Performance tests for API endpoints

3. **Frontend Testing**
   - Unit tests for components and services (90%+ coverage)
   - Integration tests for form workflows
   - E2E tests with Cypress for complete user journeys
   - Accessibility tests with axe-core

4. **Full System Testing**
   - End-to-end testing of complete registration flow
   - Password reset workflow testing
   - Email verification testing
   - Rate limiting and security testing

**Deliverables**:
- Fully integrated application
- 85%+ test coverage across all layers
- Automated test suite
- Performance benchmarks
- Security testing results

### Phase 4: Production Readiness (Week 7-8, Days 22-28)

**Objectives**: Prepare for deployment and production monitoring

**Tasks**:
1. **Deployment Configuration**
   - Docker configuration for backend and frontend
   - Standalone JAR build with embedded Tomcat
   - Environment configuration for production
   - SSL/TLS certificate setup

2. **Monitoring & Logging**
   - Application metrics with Micrometer
   - Structured logging configuration
   - Error tracking and alerting
   - Performance monitoring setup

3. **Security Hardening**
   - Final security audit
   - Penetration testing preparation
   - HIPAA compliance verification
   - Data encryption validation

4. **Documentation & Training**
   - API documentation with OpenAPI
   - User guide and deployment instructions
   - Developer onboarding documentation
   - Maintenance procedures

**Deliverables**:
- Production-ready deployment artifacts
- Complete documentation
- Monitoring and alerting setup
- Security audit results

## Testing Strategy

### Backend Testing (85%+ Coverage Target)

**Unit Tests** (JUnit 5 + Mockito):
- Service layer: PatientRegistrationService, PasswordResetService, EmailVerificationService
- Validation logic: PasswordValidationService, EmailValidationService
- Security components: RateLimitingService, RecaptchaV3Validator
- Utility classes: TokenGenerator, DisposableEmailChecker

**Integration Tests** (Spring Boot Test + H2):
- Controller endpoints with full request/response cycle
- Database operations with JPA repositories
- Email service integration (mocked)
- Security filter chains

**Security Tests**:
- Authentication flow testing
- Authorization and access control
- Rate limiting effectiveness
- Input validation and sanitization
- SQL injection prevention

**Performance Tests** (JMeter):
- API response times under load
- Concurrent user simulation
- Database query performance
- Memory usage monitoring

### Frontend Testing (85%+ Coverage Target)

**Unit Tests** (Jasmine + Karma):
- Component logic and templates
- Service methods and API calls
- Custom validators and pipes
- Guards and interceptors

**Integration Tests**:
- Form validation workflows
- Component interactions
- Service integration with mocked APIs

**E2E Tests** (Cypress):
- Complete registration flow
- Password reset workflow
- Email verification process
- Error handling scenarios
- Mobile responsiveness

**Accessibility Tests** (axe-core):
- WCAG 2.1 AA compliance
- Keyboard navigation
- Screen reader compatibility
- Color contrast validation

### Test Coverage Metrics

- **Backend**: 90%+ line coverage, 85%+ branch coverage
- **Frontend**: 90%+ line coverage, 85%+ branch coverage
- **Critical Paths**: 95%+ coverage for authentication flows
- **Security**: 100% coverage for validation and sanitization logic

### Test Automation

- **CI/CD Pipeline**: Automated testing on every commit
- **Code Quality**: SonarQube analysis with quality gates
- **Performance Regression**: Automated performance testing
- **Security Scanning**: OWASP ZAP integration

## Risk Mitigation

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| SQLite performance under load | Medium | Low | Optimize queries, use proper indexing, monitor performance |
| Email deliverability issues | High | Medium | SendGrid professional tier, proper SPF/DKIM/DMARC, bounce handling |
| reCAPTCHA integration complexity | Medium | Low | Use official Google libraries, thorough testing |
| Tailwind CSS customization | Low | Low | Follow official documentation, test across browsers |
| Standalone deployment issues | Medium | Low | Thorough testing of JAR deployment, embedded Tomcat configuration |

## Success Criteria Validation

- **SC-001**: 90% registration completion rate - Measured via E2E tests and analytics
- **SC-002**: 85% password reset success rate - Validated through integration tests
- **SC-003**: <3 minute registration time - Performance tests with realistic data
- **SC-004**: Zero security vulnerabilities - Penetration testing and code analysis
- **SC-005**: 80% reduction in support tickets - Baseline measurement and monitoring
- **SC-006**: 500 concurrent users support - Load testing with JMeter
- **SC-007**: 85% email verification rate - Email service monitoring
- **SC-008**: <500ms API response time - Performance monitoring
- **SC-009**: 95% strong password creation - A/B testing and analytics
- **SC-010**: WCAG 2.1 AA compliance - Automated accessibility testing
- **SC-011**: Mobile/desktop parity - Cross-device testing
- **SC-012**: 99%+ bot blocking - Security testing and monitoring

## Timeline & Milestones

- **Week 1**: Project setup, backend foundation, initial testing
- **Week 2**: Complete backend implementation, database setup
- **Week 3**: Frontend development, Tailwind theme implementation
- **Week 4**: Frontend completion, initial integration testing
- **Week 5**: Full system integration, comprehensive backend testing
- **Week 6**: Frontend testing, E2E test suite completion
- **Week 7**: Performance testing, security audit, production preparation
- **Week 8**: Final testing, documentation, deployment readiness

**Total Duration**: 8 weeks
**Team Size**: 2-3 developers (1 backend, 1 frontend, 1 QA)
**Risk Buffer**: 1 week for unexpected issues

## Dependencies & Prerequisites

- **External Services**: SendGrid account (HIPAA tier), Google reCAPTCHA keys
- **Development Tools**: Java 17, Maven 3.9+, Node.js 18+, SQLite 3.44+
- **Infrastructure**: Docker for containerization, CI/CD pipeline
- **Security**: SSL certificates, HIPAA compliance review
- **Testing**: Cypress for E2E, JMeter for performance, accessibility tools

## Constitution Compliance Verification

All implementation decisions align with the constitution:
- ✅ 12-Factor principles with environment-based configuration
- ✅ Clean code with SOLID principles and domain-driven design
- ✅ Comprehensive testing with 85%+ coverage target
- ✅ Minimal dependencies with core Spring Boot and Angular features
- ✅ Security-first approach with BCrypt, rate limiting, and audit logging
- ✅ Spring Boot and Angular best practices
- ✅ Responsive design with accessibility compliance
- ✅ Quality attributes prioritized (Security > Interoperability > Usability > Maintainability)</content>
<parameter name="filePath">c:\Users\Dmytro_Panov\workspace\Projects\AIinSDLC\CopilotTest\spec-kit_demo\java\forget-pass\.specify\patient-registration-password-recovery-plan.md