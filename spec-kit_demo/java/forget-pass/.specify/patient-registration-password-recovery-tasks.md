````markdown
---
description: "Tasks for Patient Registration & Password Recovery feature"
---

# Tasks: Patient Registration & Password Recovery

**Input**: `.specify/patient-registration-password-recovery-plan.md`, `patient-registration-password-recovery-spec.md`
**Prerequisites**: plan.md (research file present), spec.md (feature spec), data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)

---

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Initialize Maven Spring Boot backend project (`backend/pom.xml`, `backend/src/`)
- [ ] T002 Initialize Angular frontend project (`frontend/package.json`, `frontend/src/`) [P]
- [ ] T003 Configure Tailwind CSS theme (`frontend/tailwind.config.js`, `frontend/src/assets/styles/theme.scss`) [P]
- [ ] T004 Add CI scripts and basic checks (`.github/workflows/`, `scripts/test.sh`) [P]
- [ ] T005 Add `.specify/` docs placeholders (spec, plan, tasks, contracts)

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T010 Create database config and Flyway migrations (`backend/src/main/resources/db/V1__initial_schema.sql`)
- [ ] T011 Define JPA entities: `Patient`, `PatientCredential`, `EmailVerificationToken`, `PasswordResetToken`, `AuthenticationAuditLog` (`backend/src/main/java/com/.../model/`) 
- [ ] T012 Create repository interfaces (`backend/src/main/java/com/.../repository/`) 
- [ ] T013 Implement `PasswordEncoderConfig` and BCrypt configuration (`backend/src/main/java/com/.../security/PasswordEncoderConfig.java`)
- [ ] T014 Implement `EmailService` abstraction and SendGrid adapter (`backend/src/main/java/com/.../service/EmailService.java`, `EmailServiceSendGrid.java`)
- [ ] T015 Implement `RateLimitingService` (in-memory/Caffeine for dev) (`backend/src/main/java/com/.../service/RateLimitingService.java`)
- [ ] T016 Add security headers filter and CSRF config (`backend/src/main/java/com/.../security/SecurityHeadersFilter.java`, `WebSecurityConfig.java`)
- [ ] T017 Add Recaptcha v3 validator stub and config (`backend/src/main/java/com/.../security/RecaptchaV3Validator.java`)
- [ ] T018 Add `DisposableEmailChecker` utility and blocklist (`backend/src/main/java/com/.../util/DisposableEmailChecker.java`) [P]

---

## Phase 3: User Story 1 - New Patient Self-Registration (P1)

### Tests (write FIRST)
- [ ] T020 [P] [US1] Unit tests for `PatientRegistrationService` (`backend/src/test/java/.../PatientRegistrationServiceTest.java`)
- [ ] T021 [P] [US1] Integration test for registration flow (`backend/src/test/java/.../RegistrationFlowIT.java`)

### Implementation
- [ ] T022 [US1] Create `PatientRegistrationController` endpoints (`backend/src/main/java/.../controller/PatientRegistrationController.java`)
- [ ] T023 [US1] Implement `PatientRegistrationService` with validation and disposable email blocking (`backend/src/main/java/.../service/PatientRegistrationService.java`)
- [ ] T024 [US1] Implement DTOs for registration (`backend/src/main/java/.../dto/PatientRegistrationRequest.java`)
- [ ] T025 [US1] Add email verification token generation and persistence (`backend/src/main/java/.../service/EmailVerificationService.java`)
- [ ] T026 [US1] Frontend `RegistrationComponent` and client-side validators (`frontend/src/app/features/auth/registration/`) [P]
- [ ] T027 [US1] Wiring: API integration in `AuthenticationService` (`frontend/src/app/core/services/auth-api.service.ts`)
- [ ] T028 [US1] Accessibility and real-time validation messages (`frontend/src/app/shared/components/error-message/`) [P]

---

## Phase 4: User Story 2 - Password Reset Request (P1)

### Tests
- [ ] T030 [P] [US2] Unit tests for `PasswordResetService` (`backend/src/test/java/.../PasswordResetServiceTest.java`)
- [ ] T031 [P] [US2] Integration test for password reset end-to-end (`backend/src/test/java/.../PasswordResetFlowIT.java`)

### Implementation
- [ ] T032 [US2] Implement `PasswordResetController` endpoints (`backend/src/main/java/.../controller/PasswordResetController.java`)
- [ ] T033 [US2] Implement `PasswordResetService` with secure token generation and hashing (`backend/src/main/java/.../service/PasswordResetService.java`)
- [ ] T034 [US2] Store reset tokens hashed and add token invalidation logic (`backend/src/main/java/.../repository/PasswordResetTokenRepository.java`)
- [ ] T035 [US2] Frontend `PasswordResetRequestComponent` and `PasswordResetComponent` (`frontend/src/app/features/auth/password-reset/`) [P]
- [ ] T036 [US2] Show generic response to avoid email enumeration (`backend/src/main/java/.../controller/PasswordResetController.java`) 
- [ ] T037 [US2] Confirmation email after successful reset (`backend/src/main/java/.../service/EmailService.java`)

---

## Phase 5: User Story 3 - Email Verification and Account Activation (P2)

- [ ] T040 [US3] Implement `EmailVerificationController` (`backend/src/main/java/.../controller/EmailVerificationController.java`)
- [ ] T041 [US3] Implement resend verification with limits (3 per 24 hours) (`backend/src/main/java/.../service/EmailVerificationService.java`)
- [ ] T042 [US3] Frontend `EmailVerificationComponent` to handle token links (`frontend/src/app/features/auth/email-verification/`) [P]

---

## Phase 6: User Story 4 - Password Strength Enforcement (P2)

- [ ] T050 [US4] Implement `PasswordValidationService` (server-side) with common password checks (`backend/src/main/java/.../service/PasswordValidationService.java`)
- [ ] T051 [US4] Client-side password strength indicator component (`frontend/src/app/features/auth/password-strength-indicator/`) [P]
- [ ] T052 [US4] Prevent reuse of last 5 passwords (store previous hashes) (`backend/src/main/java/.../service/PasswordPolicyService.java`)

---

## Phase 7: User Story 5 - Rate Limiting and Security Protection (P3)

- [ ] T060 [US5] Implement rate limiting filters for registration, login, password reset (`backend/src/main/java/.../security/RateLimitingFilter.java`)
- [ ] T061 [US5] Account lockout after failed attempts (`backend/src/main/java/.../service/AccountLockService.java`)
- [ ] T062 [US5] Integrate Recaptcha v3 and threshold checks (`backend/src/main/java/.../security/RecaptchaV3Validator.java`) [P]
- [ ] T063 [US5] Add audit logging for all auth events (`backend/src/main/java/.../service/AuthenticationAuditService.java`)

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] T090 [P] Documentation updates in `docs/` and `.specify/checklists/`
- [ ] T091 Code cleanup and refactoring
- [ ] T092 Performance tuning and monitoring (Micrometer, Prometheus)
- [ ] T093 Security hardening and penetration test prep
- [ ] T094 Update `.specify/patient-registration-password-recovery-tasks.md` after implementation changes

---

## Execution Notes

- Tests should be written before implementation for critical services and user flows (T020-T021, T030-T031).
- Parallelizable tasks are marked with `[P]` and can be worked on simultaneously by different developers.
- Each task description includes exact file paths for traceability; adjust package paths to match repository structure.
- Acceptance criteria and test files reference the scenarios in `patient-registration-password-recovery-spec.md`.

````
