# Quickstart Guide: Patient Registration & Password Recovery

**Date**: 2025-11-14
**System**: Patient Authentication System
**Target Audience**: Developers, DevOps engineers

## Prerequisites

### System Requirements
- **Java**: OpenJDK 17 LTS or later
- **Maven**: 3.9+ for backend build
- **Node.js**: 18+ for frontend build
- **SQLite**: 3.44+ (included with application)
- **Git**: For version control

### External Services
- **SendGrid Account**: HIPAA-compliant tier for email delivery
- **Google reCAPTCHA**: v3 keys for bot protection

## Local Development Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd patient-auth-system
git checkout feature/001-patient-registration-password-recovery
```

### 2. Backend Setup
```bash
cd backend

# Configure environment variables
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit application.properties with your values:
# - SendGrid API key
# - Google reCAPTCHA keys
# - Database file path
# - Server port (default: 8080)

# Build the application
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### 3. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Configure environment
cp src/environments/environment.ts.example src/environments/environment.ts

# Edit environment.ts with:
# - Backend API URL
# - reCAPTCHA site key

# Start development server
npm start
```

### 4. Access Application
- **Backend API**: http://localhost:8080/api/v1
- **Frontend**: http://localhost:4200
- **API Documentation**: http://localhost:8080/swagger-ui.html

## Configuration

### Environment Variables

#### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:sqlite:patient_auth.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=com.healthcare.patientauth.config.SQLiteDialect

# Security
app.security.password.encoder.strength=12
app.security.rate-limit.requests-per-minute=5
app.security.recaptcha.secret-key=your-recaptcha-secret

# Email (SendGrid)
app.email.sendgrid.api-key=your-sendgrid-api-key
app.email.sendgrid.from-email=noreply@yourdomain.com
app.email.sendgrid.templates.verification=template-id
app.email.sendgrid.templates.password-reset=template-id

# Server
server.port=8080
server.servlet.context-path=/api/v1
```

#### Frontend (environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  recaptchaSiteKey: 'your-recaptcha-site-key',
  features: {
    enableRegistration: true,
    enablePasswordReset: true,
    enableEmailVerification: true
  }
};
```

## Database Setup

### Automatic Migration
The application uses Flyway for database migrations. On first run, it will:
1. Create the SQLite database file
2. Run migration scripts in order order
3. Initialize required data

### Manual Database Operations
```bash
# Access SQLite database
sqlite3 patient_auth.db

# View schema
.schema

# Check data
SELECT COUNT(*) FROM patients;
SELECT * FROM authentication_audit_logs LIMIT 10;
```

## Testing

### Backend Tests
```bash
cd backend
mvn test                           # Run all tests
mvn test -Dtest=PatientServiceTest # Run specific test
mvn jacoco:report                  # Generate coverage report
```

### Frontend Tests
```bash
cd frontend
npm test                           # Unit tests
npm run test:ci                    # CI mode
npm run e2e                        # End-to-end tests
```

### Full Test Suite
```bash
# Run all tests
./scripts/test.sh

# With coverage reports
./scripts/test.sh --coverage
```

## API Usage Examples

### Register New Patient
```bash
curl -X POST http://localhost:8080/api/v1/patients/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-01",
    "password": "SecurePass123!"
  }'
```

### Request Password Reset
```bash
curl -X POST http://localhost:8080/api/v1/patients/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com"}'
```

### Validate Password Strength
```bash
curl -X POST http://localhost:8080/api/v1/patients/validate/password \
  -H "Content-Type: application/json" \
  -d '{"password": "MyPassword123!"}'
```

## Deployment

### Standalone JAR
```bash
# Build production JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/patient-auth-system-1.0.0.jar \
  --spring.profiles.active=production \
  --server.port=8080
```

### Docker Deployment
```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# View logs
docker-compose logs -f backend
```

## Monitoring

### Health Checks
- **Application Health**: http://localhost:8080/api/v1/actuator/health
- **Metrics**: http://localhost:8080/api/v1/actuator/metrics
- **Info**: http://localhost:8080/api/v1/actuator/info

### Logs
```bash
# Application logs
tail -f logs/spring.log

# Access logs
tail -f logs/access.log
```

## Troubleshooting

### Common Issues

#### Database Connection Failed
```bash
# Check SQLite file permissions
ls -la patient_auth.db

# Recreate database
rm patient_auth.db
mvn flyway:migrate
```

#### Email Not Sending
```bash
# Check SendGrid configuration
curl -H "Authorization: Bearer YOUR_API_KEY" \
  https://api.sendgrid.com/v3/user/credits
```

#### reCAPTCHA Errors
```bash
# Verify site key
curl -X POST https://www.google.com/recaptcha/api/siteverify \
  -d "secret=YOUR_SECRET&response=USER_RESPONSE"
```

### Debug Mode
```bash
# Enable debug logging
java -jar target/patient-auth-system-1.0.0.jar \
  --logging.level.com.healthcare.patientauth=DEBUG
```

## Security Checklist

- [ ] Environment variables configured securely
- [ ] Database file permissions restricted
- [ ] SSL/TLS certificates installed
- [ ] Firewall rules configured
- [ ] Rate limiting active
- [ ] Audit logging enabled
- [ ] Password policies enforced

## Support

### Documentation
- **API Docs**: http://localhost:8080/swagger-ui.html
- **User Guide**: docs/user-guide/
- **Developer Guide**: docs/developer/

### Getting Help
- Check application logs
- Review health check endpoints
- Consult troubleshooting guide
- Contact development team

## Next Steps

1. Complete user acceptance testing
2. Configure production environment
3. Set up monitoring and alerting
4. Plan for scaling and high availability
5. Implement backup and disaster recovery
CREATE TABLE email_verification_tokens (
    token_id TEXT PRIMARY KEY,             -- UUID v4
    patient_id TEXT NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,              -- SHA-256 hash (64 chars)
    expires_at DATETIME NOT NULL,          -- 24-hour expiration
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at DATETIME,                  -- Verification timestamp
    resend_count INTEGER DEFAULT 0,        -- Max 3 resends per 24h
    INDEX idx_patient_id (patient_id),
    INDEX idx_expires_at (expires_at),
    CHECK (resend_count >= 0 AND resend_count <= 3)
);
```

#### Password Reset Token Table
```sql
CREATE TABLE password_reset_tokens (
    token_id TEXT PRIMARY KEY,             -- UUID v4
    patient_id TEXT NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,              -- SHA-256 hash (64 chars)
    expires_at DATETIME NOT NULL,          -- 1-hour expiration
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME,                      -- Usage timestamp
    ip_address TEXT,                       -- IPv4/IPv6 address
    user_agent TEXT,                       -- Browser/client info
    INDEX idx_patient_id (patient_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_used_at (used_at)
);
```

#### Authentication Audit Log Table
```sql
CREATE TABLE authentication_audit_logs (
    log_id TEXT PRIMARY KEY,               -- UUID v4
    patient_id TEXT REFERENCES patients(patient_id) ON DELETE SET NULL,
    event_type TEXT NOT NULL,              -- Enum: registration, login, logout, password_reset, failed_login, account_locked
    ip_address TEXT,                       -- Client IP address
    user_agent TEXT,                       -- Browser/client user agent
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL,              -- Operation success/failure
    metadata TEXT,                         -- JSON additional data
    INDEX idx_patient_id (patient_id),
    INDEX idx_event_type (event_type),
    INDEX idx_timestamp (timestamp),
    CHECK (event_type IN ('registration', 'login', 'logout', 'password_reset', 'failed_login', 'account_locked', 'email_verification'))
);
```

## Entity Relationships

```
Patient (1) ──── (1) PatientCredential
    │
    ├── (0..*) EmailVerificationToken
    ├── (0..*) PasswordResetToken
    └── (0..*) AuthenticationAuditLog
```

## Data Constraints & Business Rules

### Patient Entity
- **Age Validation**: Must be 18+ years old
- **Email Uniqueness**: One account per email address
- **Status Transitions**: pending_verification → active → locked/suspended
- **Unicode Support**: Names support international characters

### Security Constraints
- **Password History**: Prevent reuse of last 5 passwords
- **Account Lockout**: 5 failed attempts → 15-minute lockout
- **Token Expiration**: Verification (24h), Reset (1h)
- **Rate Limiting**: IP-based and account-based limits

### Audit Requirements
- **Complete Logging**: All authentication events recorded
- **Data Retention**: Configurable retention period (default 7 years for HIPAA)
- **Immutable Logs**: No updates/deletes allowed on audit records

## Migration Strategy

### Initial Schema (V1)
```sql
-- V1__Initial_schema.sql
-- Create all tables with constraints
-- Insert initial data if needed
```

### Future Migrations
- **V2**: Add password policy configuration table
- **V3**: Add email preferences table
- **V4**: Add account recovery options table

## Performance Optimizations

### Indexing Strategy
- Email lookups (unique index)
- Status-based queries (composite index)
- Time-based audit queries (timestamp index)
- Token expiration queries (expires_at index)

### Query Optimization
- Use JOIN FETCH for related entities
- Implement pagination for large result sets
- Cache frequently accessed reference data
- Use database connection pooling

## Data Validation

### Application Layer Validation
- **Email Format**: RFC 5322 compliance
- **Phone Format**: E.164 international standard
- **Password Strength**: 12+ chars, mixed case, numbers, symbols
- **Disposable Email**: Blocklist validation

### Database Constraints
- **Check Constraints**: Age, status enums, attempt counts
- **Foreign Keys**: Referential integrity
- **Unique Constraints**: Email uniqueness
- **Not Null**: Required fields enforcement

## Backup & Recovery

### SQLite Backup Strategy
- **File-based Backup**: Copy database file during low-traffic periods
- **Automated Backups**: Daily backups with 30-day retention
- **Point-in-time Recovery**: WAL mode for transaction safety

### Data Export/Import
- **CSV Export**: For data migration if needed
- **JSON Format**: For configuration and audit data
- **Encryption**: Database file encryption at rest

## Monitoring & Maintenance

### Database Metrics
- **Connection Pool**: Active/idle connections
- **Query Performance**: Slow query monitoring
- **Storage Usage**: Database file size monitoring
- **Backup Status**: Automated backup verification

### Maintenance Tasks
- **Index Rebuilding**: Periodic index optimization
- **Vacuum Operations**: SQLite database compaction
- **Log Rotation**: Audit log archival
- **Data Cleanup**: Expired token removal</content>
<parameter name="filePath">c:\Users\Dmytro_Panov\workspace\Projects\AIinSDLC\CopilotTest\spec-kit_demo\java\forget-pass\.specify\patient-registration-password-recovery-data-model.md