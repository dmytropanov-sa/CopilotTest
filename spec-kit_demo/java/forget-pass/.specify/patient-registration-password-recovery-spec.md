# Feature Specification: Patient Registration & Password Recovery

**Feature Branch**: `001-patient-registration-password-recovery`  
**Created**: 2025-11-14  
**Status**: Draft  
**Input**: User description: "Register new patient, set password, with recovering a forgotten Password feature. Currently, users can't create and reset their passwords if they forget them."

## Clarifications

### Session 2025-11-14

- Q: Should the system support registration for minors (under 18) with parental consent? → A: No minor support initially - only patients 18+ can register
- Q: Should MFA be supported in this initial release or planned for a future iteration? → A: Plan for MFA but implement in Phase 2 - architecture supports it but not enforced initially
- Q: Which email service provider should be used (SendGrid, AWS SES, Mailgun)? → A: SendGrid - HIPAA compliant option with excellent deliverability
- Q: Should the system block registration from disposable email services? → A: Block disposable emails - reject known disposable email providers
- Q: Should we use Google reCAPTCHA v3, hCaptcha, or another CAPTCHA solution? → A: Google reCAPTCHA v3 - invisible verification with risk scoring

## User Scenarios & Testing *(mandatory)*

### User Story 1 - New Patient Self-Registration (Priority: P1)

A new patient visits the healthcare platform for the first time and needs to create an account to book online visits. They must provide their personal information, create credentials, and verify their identity before accessing the platform.

**Why this priority**: This is the foundational capability - without patient registration, no other features can function. It's the entry point for all new users and directly addresses the core business need of onboarding patients to the online platform.

**Independent Test**: Can be fully tested by completing the registration form with valid patient data, creating a password, and verifying that the account is created and accessible. Delivers immediate value by allowing new patients to join the platform.

**Acceptance Scenarios**:

1. **Given** a new patient visits the registration page, **When** they fill in all required fields (first name, last name, email, phone, date of birth, password) with valid data and submit the form, **Then** the system creates a new patient account, sends a verification email, and displays a success message with next steps.

2. **Given** a new patient is on the registration page, **When** they enter a password that doesn't meet security requirements (e.g., less than 8 characters, no special characters), **Then** the system displays clear validation messages indicating which password requirements are not met and prevents form submission.

3. **Given** a new patient attempts to register, **When** they enter an email address that already exists in the system, **Then** the system displays a message "An account with this email already exists" and provides a link to the password recovery page.

4. **Given** a new patient completes registration, **When** they click the verification link in their email within 24 hours, **Then** the system activates their account and redirects them to the login page with a confirmation message.

5. **Given** a new patient is filling out the registration form, **When** they enter invalid data formats (e.g., invalid email format, future date of birth, invalid phone number), **Then** the system displays real-time validation errors next to each invalid field before form submission.

---

### User Story 2 - Password Reset Request (Priority: P1)

An existing patient has forgotten their password and cannot log in to access their appointments. They need a secure way to reset their password without contacting support.

**Why this priority**: This is equally critical as registration because patients who forget passwords are completely blocked from accessing the platform. This directly addresses the stated problem: "users can't reset their passwords if they forget them." High impact on user satisfaction and support costs.

**Independent Test**: Can be independently tested by requesting a password reset with a valid registered email, receiving the reset link, and successfully creating a new password. Delivers immediate value by restoring access to locked-out patients.

**Acceptance Scenarios**:

1. **Given** a patient is on the login page and has forgotten their password, **When** they click "Forgot Password?" and enter their registered email address, **Then** the system sends a password reset email with a secure time-limited link (valid for 1 hour) and displays "If an account exists with this email, you will receive password reset instructions."

2. **Given** a patient receives a password reset email, **When** they click the reset link within the 1-hour validity period, **Then** the system redirects them to a secure password reset page where they can enter a new password.

3. **Given** a patient is on the password reset page, **When** they enter and confirm a new password that meets all security requirements, **Then** the system updates their password, invalidates the reset token, sends a confirmation email, and redirects them to the login page with a success message.

4. **Given** a patient clicks on a password reset link, **When** the link has expired (more than 1 hour old), **Then** the system displays "This password reset link has expired. Please request a new one" and provides a button to request a new reset link.

5. **Given** a patient requests a password reset, **When** they enter an email address that doesn't exist in the system, **Then** the system still displays the same generic message "If an account exists with this email, you will receive password reset instructions" to prevent email enumeration attacks.

6. **Given** a patient with an active password reset token requests another reset, **When** they submit the new request, **Then** the system invalidates all previous reset tokens for that account and sends a new reset email with a fresh token.

---

### User Story 3 - Email Verification and Account Activation (Priority: P2)

A newly registered patient needs to verify their email address before they can fully access the platform's features, ensuring that the contact information is valid and the patient has access to the provided email.

**Why this priority**: Email verification is important for security and communication reliability, but the core registration can be completed without it. Patients could have limited access until verified, making this secondary to the basic registration and password reset flows.

**Independent Test**: Can be tested by registering a new account, receiving the verification email, and clicking the verification link to activate the account. Delivers value by ensuring valid patient contact information.

**Acceptance Scenarios**:

1. **Given** a patient completes registration, **When** the account is created, **Then** the system sends a verification email with a unique activation link valid for 24 hours and marks the account status as "pending verification."

2. **Given** a patient receives a verification email, **When** they click the activation link within 24 hours, **Then** the system activates the account, changes status to "active," and displays a confirmation message.

3. **Given** a patient with an unverified account, **When** they attempt to log in, **Then** the system allows login but displays a banner "Please verify your email address" and provides an option to resend the verification email.

4. **Given** a patient's verification link has expired, **When** they click the expired link, **Then** the system displays "This verification link has expired" and provides a button to request a new verification email.

5. **Given** a patient with an unverified account, **When** they request to resend the verification email (maximum 3 times per 24 hours), **Then** the system sends a new verification email with a fresh token and displays "Verification email sent."

---

### User Story 4 - Password Strength Enforcement and Security (Priority: P2)

Patients need to create strong, secure passwords that protect their sensitive health information, with clear guidance on password requirements and real-time feedback during password creation.

**Why this priority**: While critical for security (as mandated by the constitution), this is a supporting feature that enhances the registration and password reset flows rather than being standalone functionality. It should be implemented as part of P1 stories.

**Independent Test**: Can be tested by attempting to create passwords with various strength levels during registration or password reset and verifying that weak passwords are rejected with helpful feedback.

**Acceptance Scenarios**:

1. **Given** a patient is creating a password during registration or reset, **When** they type in the password field, **Then** the system displays a real-time password strength indicator (Weak/Medium/Strong) and lists which requirements are met or not met.

2. **Given** a patient creates a password, **When** the password is less than 12 characters, contains no special characters, or lacks uppercase/lowercase/numbers, **Then** the system prevents form submission and displays specific requirements not met.

3. **Given** a patient is setting a new password, **When** they enter their email address, name, or common passwords (from a known weak password list), **Then** the system rejects the password with the message "Password is too common or contains personal information."

4. **Given** a patient is resetting their password, **When** they enter their current password as the new password, **Then** the system rejects it with "New password must be different from your previous password."

5. **Given** a patient creates a strong password (12+ chars, uppercase, lowercase, numbers, special characters), **When** they submit the form, **Then** the system accepts the password and provides visual feedback that it meets all requirements.

---

### User Story 5 - Rate Limiting and Security Protection (Priority: P3)

The system must protect against automated attacks and abuse by implementing rate limiting on registration, login, and password reset requests, while maintaining a good user experience for legitimate patients.

**Why this priority**: Important for security and system stability but not immediately blocking user functionality. Can be implemented after core flows are working, as the constitution mandates security but allows phased implementation.

**Independent Test**: Can be tested by making multiple rapid requests to registration, login, or password reset endpoints and verifying that rate limits are enforced after thresholds are exceeded.

**Acceptance Scenarios**:

1. **Given** a user makes multiple password reset requests, **When** they exceed 5 requests within 15 minutes from the same IP address, **Then** the system temporarily blocks further requests for 15 minutes and displays "Too many requests. Please try again later."

2. **Given** a user attempts multiple failed login attempts, **When** they exceed 5 failed attempts within 15 minutes, **Then** the system locks the account for 15 minutes and sends a security alert email to the registered address.

3. **Given** multiple registration attempts from the same IP address, **When** they exceed 10 registrations within 1 hour, **Then** the system requires CAPTCHA verification for subsequent registration attempts from that IP.

4. **Given** a legitimate user is temporarily rate-limited, **When** the rate limit period expires, **Then** the system automatically restores full access without requiring manual intervention.

---

### Edge Cases

- **What happens when a patient's verification email bounces (invalid email address)?**
  - System logs the bounce and marks the account as "email_invalid"
  - Patient can update their email address through support or by re-registering
  - Admin dashboard shows accounts with invalid emails for follow-up

- **How does the system handle concurrent password reset requests?**
  - Only the most recent reset token remains valid
  - All previous tokens for the same account are automatically invalidated
  - Each reset attempt is logged for audit trail

- **What happens if a patient clicks multiple verification links?**
  - First successful verification activates the account
  - Subsequent clicks on already-verified accounts show "Account already verified" message
  - Expired tokens show appropriate expiry message

- **How does the system handle special characters in names (e.g., O'Brien, José, 李明)?**
  - System accepts Unicode characters and common punctuation in name fields
  - Input validation allows letters, spaces, hyphens, apostrophes, and diacritics
  - Database uses UTF-8 encoding to properly store international characters

- **What happens during registration if email service is temporarily unavailable?**
  - System creates the account with "pending_verification" status
  - Queues the verification email for retry (3 attempts over 1 hour)
  - Patient sees success message but banner indicates email may be delayed
  - Background job retries email delivery

- **How does the system prevent timing attacks on password reset?**
  - Same response time for valid and invalid email addresses
  - Generic success message regardless of email existence
  - Constant-time comparison for password reset tokens

- **What happens if a patient tries to register during a database outage?**
  - System returns user-friendly error message: "We're experiencing technical difficulties. Please try again in a few minutes."
  - Error is logged for monitoring and alerting
  - Patient's input data is not lost (frontend preserves form state)

- **How does the system handle password reset for accounts with multiple email addresses or phone numbers?**
  - Password reset sent only to the primary email address on file
  - System displays which email address the reset link was sent to (partially masked: j***@example.com)
  - Patient can contact support if they no longer have access to primary email

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow new patients to self-register by providing: first name, last name, email address, phone number, date of birth, and password.

- **FR-002**: System MUST validate email addresses using RFC 5322 standard format and verify uniqueness before account creation.

- **FR-003**: System MUST enforce password requirements: minimum 12 characters, at least one uppercase letter, one lowercase letter, one number, one special character, and not matching common weak passwords.

- **FR-004**: System MUST hash passwords using BCrypt with a cost factor of at least 12 before storage; passwords MUST NEVER be stored in plain text.

- **FR-005**: System MUST send email verification to new registrants with a time-limited token (24-hour expiration).

- **FR-006**: System MUST allow existing patients to request password reset by entering their registered email address.

- **FR-007**: System MUST send password reset emails with secure, single-use tokens valid for 1 hour only.

- **FR-008**: System MUST invalidate password reset tokens after successful use or when a new reset is requested.

- **FR-009**: System MUST prevent email enumeration by returning the same generic message for both existing and non-existing email addresses during password reset.

- **FR-010**: System MUST provide real-time password strength feedback during password entry (weak/medium/strong indicator).

- **FR-011**: System MUST validate date of birth to ensure patients are at least 18 years old. Patients under 18 are not supported in the initial release.

- **FR-012**: System MUST log all authentication-related events (registration, login attempts, password resets) for security audit trail.

- **FR-013**: System MUST implement rate limiting: max 5 password reset requests per 15 minutes per IP address, max 5 failed login attempts per 15 minutes per account.

- **FR-014**: System MUST send confirmation emails after successful password changes for security notification.

- **FR-015**: System MUST support email resend functionality for verification emails (max 3 resends per 24 hours per account).

- **FR-016**: System MUST implement Google reCAPTCHA v3 for all registration requests with risk score threshold of 0.5 (scores below 0.5 are rejected as likely bots). No explicit user interaction required.

- **FR-017**: System MUST validate phone numbers using E.164 international format and accept common separators (spaces, dashes, parentheses).

- **FR-018**: System MUST prevent registration with disposable email addresses by validating against a maintained blocklist of known disposable email providers (e.g., 10minutemail.com, guerrillamail.com, tempmail.org). Display clear error message: "Please use a permanent email address for registration."

- **FR-019**: System MUST provide accessible error messages that comply with WCAG 2.1 AA standards and guide users toward resolution.

- **FR-020**: System MUST implement Cross-Site Request Forgery (CSRF) protection on all forms.

### Non-Functional Requirements

- **NFR-001**: Password reset emails MUST be delivered within 60 seconds under normal conditions (95th percentile).

- **NFR-002**: Registration API endpoint MUST respond within 500ms for 95% of requests under normal load.

- **NFR-003**: System MUST support at least 100 concurrent registration requests without degradation.

- **NFR-004**: All password-related operations MUST be performed over HTTPS/TLS 1.3 only.

- **NFR-005**: Email verification and password reset tokens MUST be cryptographically secure (minimum 32 bytes of entropy).

- **NFR-006**: System MUST maintain 99.9% uptime for authentication services.

- **NFR-007**: All forms MUST be mobile-responsive and functional on devices with minimum 320px width.

- **NFR-008**: Registration and password reset flows MUST be keyboard-navigable and screen-reader accessible.

- **NFR-009**: System MUST comply with HIPAA requirements for patient data protection and audit logging.

- **NFR-010**: Password reset tokens MUST be stored hashed in the database, not in plain text.

### Security Requirements

- **SEC-001**: System MUST implement Content Security Policy (CSP) headers to prevent XSS attacks.

- **SEC-002**: System MUST use parameterized queries or ORM to prevent SQL injection attacks.

- **SEC-003**: System MUST implement proper session management with secure, HttpOnly, SameSite cookies.

- **SEC-004**: System MUST sanitize all user input on both frontend and backend to prevent XSS attacks.

- **SEC-005**: System MUST implement proper CORS configuration to allow only authorized origins.

- **SEC-006**: System MUST log security events (failed logins, password resets, account lockouts) with IP addresses and timestamps.

- **SEC-007**: System MUST implement account lockout after 5 consecutive failed login attempts.

- **SEC-008**: System MUST send security notification emails for sensitive actions (password changes, email changes).

- **SEC-009**: System MUST validate all password reset tokens against expiration time, usage count, and account association before allowing password change.

- **SEC-010**: System MUST implement proper error handling that doesn't leak sensitive information in error messages.

### Key Entities

- **Patient**: Represents a registered user of the healthcare platform
  - Attributes: patientId (UUID), firstName, lastName, email (unique, indexed), phoneNumber, dateOfBirth, accountStatus (active/pending_verification/locked), createdAt, updatedAt, lastLoginAt
  - Relationships: Has many Appointments, has one PatientCredential, has many AuditLogs

- **PatientCredential**: Stores authentication credentials for a patient
  - Attributes: credentialId (UUID), patientId (FK), passwordHash (BCrypt), passwordSalt, passwordChangedAt, previousPasswordHashes (array of last 5 passwords to prevent reuse), failedLoginAttempts, lockedUntil
  - Relationships: Belongs to Patient

- **EmailVerificationToken**: Manages email verification during registration
  - Attributes: tokenId (UUID), patientId (FK), tokenHash (SHA-256), expiresAt, createdAt, verifiedAt, resendCount
  - Relationships: Belongs to Patient

- **PasswordResetToken**: Manages password reset requests
  - Attributes: tokenId (UUID), patientId (FK), tokenHash (SHA-256), expiresAt, createdAt, usedAt, ipAddress, userAgent
  - Relationships: Belongs to Patient

- **AuthenticationAuditLog**: Records all authentication-related events
  - Attributes: logId (UUID), patientId (FK, nullable), eventType (registration/login/logout/password_reset/failed_login), ipAddress, userAgent, timestamp, success (boolean), metadata (JSON)
  - Relationships: Belongs to Patient (if authenticated)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 90% of new patients successfully complete registration process on first attempt without errors.

- **SC-002**: Password reset requests result in successful password change within 5 minutes for 85% of users.

- **SC-003**: Registration process completion time averages under 3 minutes from landing page to email verification.

- **SC-004**: Zero security incidents related to authentication vulnerabilities within first 6 months of launch.

- **SC-005**: Support tickets related to password issues decrease by 80% compared to manual password reset process.

- **SC-006**: System handles peak load of 500 concurrent registrations during marketing campaigns without degradation.

- **SC-007**: Email verification rate reaches 85% within 24 hours of registration.

- **SC-008**: API response times for registration and password reset remain under 500ms for 95th percentile.

- **SC-009**: Password strength indicator leads to 95% of users creating "Strong" passwords meeting all requirements.

- **SC-010**: Accessibility audit confirms WCAG 2.1 AA compliance with zero critical issues.

- **SC-011**: Mobile registration completion rate matches desktop (within 5% variance).

- **SC-012**: System successfully blocks 99%+ of automated bot registration attempts via rate limiting and CAPTCHA.

## Technical Considerations

### Backend (Spring Boot) Components

**Controllers**:
- `PatientRegistrationController`: Handles registration endpoints
- `PasswordResetController`: Manages password reset flow
- `EmailVerificationController`: Processes email verification

**Services**:
- `PatientRegistrationService`: Business logic for patient registration
- `PasswordResetService`: Password reset orchestration
- `EmailVerificationService`: Email verification logic
- `PasswordValidationService`: Password strength validation and policy enforcement
- `EmailValidationService`: Email format and disposable domain validation
- `EmailService`: Email delivery abstraction
- `RateLimitingService`: Rate limiting implementation

**Repositories**:
- `PatientRepository`: Patient entity data access
- `PatientCredentialRepository`: Credential management
- `PasswordResetTokenRepository`: Reset token storage
- `EmailVerificationTokenRepository`: Verification token storage
- `AuthenticationAuditLogRepository`: Audit trail storage

**Security Components**:
- `PasswordEncoderConfig`: BCrypt configuration (strength 12)
- `RateLimitingFilter`: Request throttling filter
- `RecaptchaV3Validator`: Google reCAPTCHA v3 integration with configurable threshold
- `SecurityHeadersFilter`: CSP, HSTS, X-Frame-Options headers

### Frontend (Angular) Components

**Feature Module**: `PatientAuthModule` (lazy-loaded)

**Components**:
- `RegistrationComponent`: Patient registration form
- `PasswordResetRequestComponent`: Request password reset
- `PasswordResetComponent`: Set new password
- `EmailVerificationComponent`: Email verification handler
- `PasswordStrengthIndicatorComponent`: Visual password strength feedback

**Services**:
- `AuthenticationService`: API communication for auth operations
- `PasswordValidationService`: Client-side password validation
- `FormValidationService`: Reusable form validators

**Guards**:
- `AnonymousGuard`: Prevent authenticated users from accessing registration/reset pages

**Interceptors**:
- `CsrfInterceptor`: CSRF token handling
- `ErrorHandlerInterceptor`: Centralized error handling

### Integration Points

- **Email Service**: SendGrid integration for transactional emails (HIPAA compliant tier)
- **Rate Limiting**: Redis-backed rate limiting for distributed systems
- **Logging**: ELK Stack for centralized security audit logging
- **Monitoring**: Prometheus metrics for registration/authentication events
- **CAPTCHA**: Google reCAPTCHA v3 (invisible, risk score-based bot detection)

### Testing Strategy

**Backend Testing**:
- Unit tests for all service layer logic (JUnit 5, Mockito)
- Integration tests with TestContainers (PostgreSQL, Redis)
- Security tests for authentication flows
- Contract tests for API endpoints (Spring Cloud Contract)
- Performance tests for registration under load (JMeter)

**Frontend Testing**:
- Unit tests for components and services (Jasmine, Karma)
- Integration tests for form workflows
- E2E tests for complete registration and password reset flows (Cypress)
- Accessibility tests (axe-core)
- Visual regression tests (Percy)

**Test Coverage Target**: 85%+ overall coverage with focus on critical security paths at 95%+

### Database Schema (High-Level)

```sql
-- Note: Actual implementation will use Flyway migrations

CREATE TABLE patients (
    patient_id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE NOT NULL,
    account_status VARCHAR(50) NOT NULL DEFAULT 'pending_verification',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_account_status (account_status)
);

CREATE TABLE patient_credentials (
    credential_id UUID PRIMARY KEY,
    patient_id UUID UNIQUE NOT NULL REFERENCES patients(patient_id),
    password_hash VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    previous_password_hashes TEXT[], -- Last 5 hashes
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE email_verification_tokens (
    token_id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(patient_id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    verified_at TIMESTAMP,
    resend_count INT DEFAULT 0,
    INDEX idx_patient_id (patient_id),
    INDEX idx_expires_at (expires_at)
);

CREATE TABLE password_reset_tokens (
    token_id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(patient_id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    INDEX idx_patient_id (patient_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_used_at (used_at)
);

CREATE TABLE authentication_audit_logs (
    log_id UUID PRIMARY KEY,
    patient_id UUID REFERENCES patients(patient_id),
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    success BOOLEAN NOT NULL,
    metadata JSONB,
    INDEX idx_patient_id (patient_id),
    INDEX idx_event_type (event_type),
    INDEX idx_timestamp (timestamp)
);
```

## API Endpoints (High-Level)

### Registration Endpoints

```
POST /api/v1/patients/register
- Request: { firstName, lastName, email, phoneNumber, dateOfBirth, password }
- Response: 201 Created, { patientId, email, status: "pending_verification" }

POST /api/v1/patients/verify-email
- Request: { token }
- Response: 200 OK, { message: "Email verified successfully" }

POST /api/v1/patients/resend-verification
- Request: { email }
- Response: 200 OK, { message: "Verification email sent" }
```

### Password Reset Endpoints

```
POST /api/v1/patients/password-reset/request
- Request: { email }
- Response: 200 OK, { message: "If account exists, reset instructions sent" }

POST /api/v1/patients/password-reset/validate-token
- Request: { token }
- Response: 200 OK, { valid: true/false }

POST /api/v1/patients/password-reset/confirm
- Request: { token, newPassword }
- Response: 200 OK, { message: "Password reset successfully" }
```

### Validation Endpoints

```
POST /api/v1/patients/validate/email
- Request: { email }
- Response: 200 OK, { available: true/false }

POST /api/v1/patients/validate/password
- Request: { password }
- Response: 200 OK, { strength: "weak/medium/strong", requirements: {} }
```

## Risks and Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Email deliverability issues (spam filters) | High | Medium | Use SendGrid with proper SPF/DKIM/DMARC configuration, monitor bounce rates via SendGrid webhooks, maintain sender reputation |
| Automated bot registrations | Medium | High | Implement rate limiting, CAPTCHA, email verification, honeypot fields |
| Password reset token exploitation | High | Low | Short expiration (1 hour), single-use tokens, rate limiting, audit logging |
| Database performance under load | Medium | Medium | Proper indexing, connection pooling, query optimization, caching |
| GDPR/HIPAA compliance violations | High | Low | Legal review, proper data handling, audit trails, encryption at rest and in transit |
| Weak passwords despite validation | Medium | Medium | Strong password policy, password strength indicator, reject common passwords |
| Account takeover via password reset | High | Low | Email verification, security notifications, rate limiting, audit logging |

## Open Questions

1. **Social Login**: Should the system support OAuth login with Google/Facebook/Apple, or only email/password authentication?

2. **Password Expiration**: Should passwords expire after a certain period (e.g., 90 days), or is that not required for patient accounts?

3. **Account Recovery**: If a patient loses access to their registered email, what recovery process should be available (phone verification, support contact)?

4. **International Phone Numbers**: What countries' phone number formats must be supported?

5. **Data Retention**: How long should authentication audit logs be retained before archival or deletion?

## Dependencies

- SendGrid account with HIPAA compliant tier configured (API key, domain verification, SPF/DKIM/DMARC setup)
- Google reCAPTCHA v3 site key and secret key (register domain at google.com/recaptcha)
- Redis instance for rate limiting (can start with in-memory for development)
- Database (PostgreSQL) with proper schema setup
- SSL/TLS certificates for HTTPS
- Frontend hosting with HTTPS support
- Monitoring and logging infrastructure (ELK Stack, Prometheus)

## Timeline Estimate

- **Phase 1** (Sprint 1): Basic patient registration with password creation - P1 User Story 1 (2 weeks)
- **Phase 2** (Sprint 2): Email verification and password reset - P1 User Story 2, P2 User Story 3 (2 weeks)
- **Phase 3** (Sprint 3): Password strength enforcement, validation, security hardening - P2 User Story 4 (1 week)
- **Phase 4** (Sprint 4): Rate limiting, CAPTCHA, final security features - P3 User Story 5 (1 week)
- **Phase 5** (Sprint 5): E2E testing, accessibility audit, performance testing (1 week)
- **Phase 6** (Sprint 6): Security audit, penetration testing, production readiness (1 week)
- **Phase 7** (Future Release): Multi-Factor Authentication implementation (planned for Phase 2 - architecture designed to support MFA without major refactoring)

**Total Estimated Duration**: 8 weeks (6 sprints + buffer) for initial release

## Compliance Checklist

- [ ] HIPAA compliance review for patient data handling
- [ ] GDPR compliance for data privacy (if serving EU patients)
- [ ] WCAG 2.1 AA accessibility audit
- [ ] OWASP Top 10 security verification
- [ ] Penetration testing by security team
- [ ] Legal review of terms of service and privacy policy
- [ ] Infrastructure security audit (HTTPS, headers, certificates)
- [ ] Data encryption at rest verification
- [ ] Audit logging completeness verification
- [ ] Disaster recovery and backup procedures documented
