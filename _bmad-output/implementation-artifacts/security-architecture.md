---
stepsCompleted: [1, 2]
inputDocuments:
  - security-audit-report.md
  - project-context.md
  - prd.md
workflowType: 'security-architecture'
project_name: 'fcode project'
user_name: 'Admin'
date: '2026-01-25'
status: 'complete'
completedAt: '2026-01-25'
---

# Security Architecture Document

**Project:** Game Account Shop
**Purpose:** Security remediation and vulnerability fix planning
**Created:** 2026-01-25
**Author:** Winston (Architect) + Admin

---

## Executive Summary

This document addresses critical and high-priority security vulnerabilities discovered during a comprehensive security audit of the Game Account Shop platform. The audit examined 10 OWASP-style vulnerability categories and identified **3 critical**, **3 high-priority**, and **4 medium-priority** issues requiring remediation.

**Security Risk Profile:**
- **Critical Issues:** 3 (Hardcoded credentials, weak default password, unencrypted game account passwords)
- **High Priority:** 3 (CSRF coverage, BCrypt strength, service-layer authorization)
- **Medium Priority:** 4 (Dependency updates, debug logging, IDOR verification, generic error messages)
- **Secure:** 4 (SQL Injection, XSS, IDOR basics, Insecure Deserialization)

---

## Current Security Posture Assessment

### What's Working Well ✅

| Area | Status | Details |
|------|--------|---------|
| SQL Injection Prevention | ✅ Secure | Spring Data JPA with `@Param` for all queries |
| XSS Protection | ✅ Secure | Thymeleaf auto-escapes all output |
| IDOR (Basic) | ✅ Secure | Repository queries filter by ownership |
| Insecure Deserialization | ✅ Secure | No unsafe deserialization patterns found |
| Password Hashing | ✅ Secure | BCrypt with 10 rounds (needs increase to 12+) |

### Critical Vulnerabilities Requiring Immediate Fix 🔴

#### 1. Hardcoded Credentials (CRITICAL)

**Locations:**
- `application.yml:8-9` - Database root password: `123456`
- `application.yml:53-54` - Email credentials exposed
- `application.yml:90-92` - PayOS API keys exposed
- `application.yml:86` - ImgBB API key exposed

**Risk:** Source code repository compromise exposes production credentials, allowing unauthorized database access, payment fraud, and data breach.

**Impact:** CRITICAL - Full system compromise

#### 2. Weak Default Admin Password (CRITICAL)

**Location:** `DataInitializer.java:37-49`
```java
admin.setPassword(passwordEncoder.encode("admin123"));
```

**Risk:** Predictable admin credentials allow immediate admin access upon deployment.

**Impact:** CRITICAL - Administrative takeover

#### 3. Game Account Passwords Stored Unencrypted (CRITICAL)

**Location:** Database storage of game account credentials

**Risk:** Database breach exposes all game account passwords, enabling account theft and secondary fraud.

**Impact:** CRITICAL - Customer data breach, platform trust destroyed

---

## Security Architecture Decisions

### Decision 1: Secrets Management Strategy

**Status:** ✅ DECIDED

**Selected Approach:** Option A - Environment Variables

**Context:** Current application has all sensitive credentials hardcoded in `application.yml` which is checked into source control.

**Deployment Context:**
- Environment: VPS / Single Server
- Timeline: Deploying to Production Soon
- Expertise Level: Beginner

**Decision Rationale:**
- Simple implementation for beginner infrastructure level
- No additional infrastructure complexity
- Works well with VPS deployment
- 12-factor app compliant
- Clear migration path to Vault/KMS when scaling

**Implementation Plan:**

1. **Update `application.yml`** - Replace hardcoded values:
   ```yaml
   spring:
     datasource:
       username: ${DB_USERNAME:root}
       password: ${DB_PASSWORD}
   mail:
     username: ${EMAIL_USERNAME}
     password: ${EMAIL_PASSWORD}
   payos:
     client-id: ${PAYOS_CLIENT_ID}
     api-key: ${PAYOS_API_KEY}
     checksum-key: ${PAYOS_CHECKSUM_KEY}
   imgbb:
     api-key: ${IMGBB_API_KEY}
   ```

2. **Create `.env.example`** (safe to commit):
   ```bash
   DB_USERNAME=root
   DB_PASSWORD=your_secure_password_here
   EMAIL_USERNAME=your_email@gmail.com
   EMAIL_PASSWORD=your_app_password_here
   PAYOS_CLIENT_ID=your_payos_client_id
   PAYOS_API_KEY=your_payos_api_key
   PAYOS_CHECKSUM_KEY=your_payos_checksum_key
   IMGBB_API_KEY=your_imgbb_api_key
   ```

3. **Create `.env`** (add to `.gitignore`):
   - Copy `.env.example` to `.env`
   - Fill in actual values
   - Never commit `.env` to repository

4. **Update deployment script**:
   ```bash
   # Before starting application
   source .env
   java -jar target/game-account-shop.jar
   ```

5. **Credential Rotation** - CRITICAL:
   - Change database password immediately after migration
   - Generate new PayOS API keys
   - Create new Gmail app-specific password
   - Rotate ImgBB API key

**Migration Path:**
- Phase 1 (MVP): Environment variables
- Phase 2 (Scaling): Spring Cloud Config or HashiCorp Vault

---

### Decision 2: Game Account Password Encryption

**Status:** ✅ DECIDED

**Selected Approach:** Spring Security Crypto with Key Rotation Support

**Context:** Game account passwords (credentials buyers receive) are currently stored in plain text in the database.

**Risk Assessment:**
- Sensitivity: **HIGH** - Critical business risk
- Database breach would expose all game account credentials
- Platform trust would be destroyed
- Legal and liability implications

**Decision Rationale:**
- High sensitivity requires encryption before production launch
- Key rotation capability needed for future-proofing
- Spring Security Crypto provides balance of simplicity and security
- Rotation support allows gradual migration to new keys

**Implementation:**

```java
@Service
public class EncryptionService {
    private final Map<String, StringEncryptor> encryptors = new ConcurrentHashMap<>();
    private final String currentKeyId;

    @PostConstruct
    public void init() {
        String currentKey = System.getenv("ENCRYPTION_CURRENT_KEY");
        String previousKey = System.getenv("ENCRYPTION_PREVIOUS_KEY");

        this.currentKeyId = System.getenv("ENCRYPTION_KEY_ID");
        encryptors.put(currentKeyId, Encryptors.text(currentKey, "a1b2c3d4"));

        if (previousKey != null) {
            encryptors.put(System.getenv("ENCRYPTION_PREVIOUS_KEY_ID"),
                          Encryptors.text(previousKey, "a1b2c3d4"));
        }
    }

    // Format: keyId:encryptedData
    public String encrypt(String plainText) {
        String encrypted = encryptors.get(currentKeyId).encrypt(plainText);
        return currentKeyId + ":" + encrypted;
    }

    public String decrypt(String encryptedWithKeyId) {
        String[] parts = encryptedWithKeyId.split(":", 2);
        return encryptors.get(parts[0]).decrypt(parts[1]);
    }
}
```

**Environment Variables Required:**
```bash
ENCRYPTION_CURRENT_KEY=32-char-random-string
ENCRYPTION_KEY_ID=2025-01
ENCRYPTION_PREVIOUS_KEY=  # Optional, for rotation
ENCRYPTION_PREVIOUS_KEY_ID=  # Optional, for rotation
```

**Database Migration Required:**
- Encrypt existing plain text passwords
- Add migration script with rollback
- Update GameAccount entity

**Priority:** CRITICAL - Must be fixed before production launch

---

### Decision 3: Authentication Security Hardening

**Status:** ✅ DECIDED

**Selected Approach:** Environment Variable Admin Password + BCrypt 12

**Context:** Current BCrypt work factor is 10 (below OWASP 12+ recommendation) and default admin password `admin123` is weak.

**Decision Rationale:**
- Active admin use requires secure default credentials
- Environment variable reset avoids UI complexity for MVP
- BCrypt increase is simple, high-value security improvement
- Console output fallback ensures admin access is never lost

**Implementation:**

**Fix 1: Increase BCrypt Work Factor**
```java
// SecurityConfig.java:26
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Up from 10
}
```

**Fix 2: Admin Password via Environment Variable**
```java
// DataInitializer.java
@Component
public class DataInitializer implements ApplicationRunner {
    @Value("${app.admin.initial-password:}")
    private String initialPassword;

    @Override
    public void run(ApplicationArguments args) {
        String adminPassword = initialPassword;

        if (adminPassword.isBlank()) {
            // Generate 16-character random password
            adminPassword = UUID.randomUUID().toString().replace("-", "");
            log.warn("============================================");
            log.warn("ADMIN INITIAL PASSWORD: {}", adminPassword);
            log.warn("Set ADMIN_INITIAL_PASSWORD env var to use custom password");
            log.warn("============================================");
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
```

**Configuration:**
```yaml
# application.yml
app:
  admin:
    initial-password: ${ADMIN_INITIAL_PASSWORD:}
```

**Password Reset Flow:**
1. Set `ADMIN_INITIAL_PASSWORD` environment variable
2. Restart application
3. New password encoded and saved to database
4. Clear environment variable (optional)

**Priority:** HIGH - Simple changes, significant security improvement

---

### Decision 4: CSRF Protection Strategy

**Status:** ✅ DECIDED

**Selected Approach:** Dual-Strategy (CSRF for Web, API Keys for External Clients)

**Context:**
- CSRF protection partially implemented
- Significant AJAX usage requiring CSRF tokens in headers
- External API clients that cannot use CSRF tokens
- Small number of endpoints to update (under 10)

**Decision Rationale:**
- Web browsers need CSRF tokens for protection
- API clients need API key authentication instead
- Dual approach allows both client types
- Small scope allows manual token addition

**Implementation:**

**1. Add CSRF Meta Tags to Base Template**
```html
<head>
    <meta name="_csrf" th:content="${_csrf.token}"/>
    <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
</head>
```

**2. Spring Security Configuration**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/api/**") // API uses API keys
        );
    return http.build();
}
```

**3. API Key Filter for External Clients**
```java
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    @Value("${app.api.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            String apiKey = request.getHeader("X-API-Key");
            if (!validApiKey.equals(apiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
```

**4. AJAX Helper for CSRF**
```javascript
function securedFetch(url, options = {}) {
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    options.headers = options.headers || {};
    options.headers[csrfHeader] = csrfToken;

    return fetch(url, options);
}
```

**Configuration:**
```yaml
app:
  api:
    key: ${API_KEY:your-secure-api-key-here}
```

**Endpoints to Update:**
- Add CSRF tokens to all forms in templates
- Add meta tags to base layout
- Implement API key filter
- Update AJAX calls to use securedFetch

**Priority:** HIGH - Protects against CSRF attacks on authenticated users

---

### Decision 5: Service-Layer Authorization

**Status:** ✅ DECIDED

**Selected Approach:** Controller-First with Critical Service Protection

**Context:**
- Authorization exists at controller level only
- Only HTTP controllers call services (no non-HTTP entry points)
- Small scope (under 10 methods need review)

**Decision Rationale:**
- Current controller-only approach is acceptable since no other entry points exist
- Adding service-layer protection for critical operations provides defense-in-depth
- Small scope makes implementation manageable
- Pragmatic approach balances security with development time

**Implementation:**

**Phase 1: Enable Method Security (One-time Setup)**
```java
// SecurityConfig.java - Add class-level annotation
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    // ... existing configuration
}
```

**Phase 2: Add @PreAuthorize to Critical Service Methods**
```java
@Service
public class GameAccountService {

    // ADMIN ONLY - Critical operations
    @PreAuthorize("hasRole('ADMIN')")
    public void approveListing(Long id) { }

    @PreAuthorize("hasRole('ADMIN')")
    public void rejectListing(Long id, String reason) { }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteListing(Long id) { }

    // OWNERSHIP CHECK - User can only access their data
    @PreAuthorize("#sellerId == authentication.principal.id")
    public void updateListing(Long id, Long sellerId, ListingUpdateRequest request) { }

    // PUBLIC - No authorization needed
    public ListingDetailDto getListingDetail(Long id) { }
}
```

**Quick Reference: @PreAuthorize Patterns**
| Pattern | Meaning | Use For |
|---------|---------|---------|
| `hasRole('ADMIN')` | User must have ADMIN role | Admin operations |
| `hasAnyRole('ADMIN', 'SELLER')` | User has one of these roles | Multi-role access |
| `#userId == authentication.principal.id` | User can only access their own data | Ownership checks |

**Methods Requiring Service-Layer Authorization:**
- `GameAccountService.approveListing()` - ADMIN only
- `GameAccountService.rejectListing()` - ADMIN only
- `GameAccountService.deleteListing()` - ADMIN only
- `GameAccountService.updateListing()` - Ownership check
- `PayoutService.markAsReceived()` - Ownership check

**Priority:** MEDIUM - Defense in depth, but current controller-layer auth is acceptable

---

## Implementation Plan

### Priority Matrix

| Priority | Decision | Risk Level | Complexity | Dependencies |
|----------|----------|------------|------------|--------------|
| **P0** | Secrets Management (Decision 1) | Critical | Low | None |
| **P0** | Game Account Encryption (Decision 2) | Critical | Medium | None |
| **P1** | BCrypt Increase (Decision 3) | High | Very Low | None |
| **P1** | Admin Password Security (Decision 3) | High | Low | None |
| **P1** | CSRF Protection (Decision 4) | High | Medium | API Key setup |
| **P2** | Service-Layer Auth (Decision 5) | Medium | Low | @EnableMethodSecurity |

### Implementation Order

**Phase 1: Critical Fixes (Before Production Launch)**
1. Move all secrets to environment variables (Decision 1)
2. Implement game account password encryption (Decision 2)
3. Generate new secure credentials (rotate all exposed keys)

**Phase 2: High Priority Security (Within Security Sprint)**
4. Increase BCrypt work factor to 12 (Decision 3)
5. Implement environment variable admin password (Decision 3)
6. Complete CSRF token coverage (Decision 4)

**Phase 3: Security Hardening (Ongoing)**
7. Add service-layer @PreAuthorize to critical methods (Decision 5)
8. Implement API key authentication for external clients (Decision 4)

### Technical Implementation Checklist

**Decision 1: Secrets Management**
- [ ] Update `application.yml` with environment variable placeholders
- [ ] Create `.env.example` file (safe to commit)
- [ ] Create `.env` file (add to `.gitignore`)
- [ ] Update deployment scripts to load environment variables
- [ ] Rotate database password
- [ ] Rotate PayOS API keys
- [ ] Rotate email app password
- [ ] Rotate ImgBB API key

**Decision 2: Game Account Password Encryption**
- [ ] Add `spring-security-crypto` dependency to `pom.xml`
- [ ] Create `EncryptionService` with key rotation support
- [ ] Update `GameAccount` entity for encrypted passwords
- [ ] Create database migration script
- [ ] Encrypt all existing plain text passwords
- [ ] Update password delivery flow to decrypt
- [ ] Set `ENCRYPTION_CURRENT_KEY` environment variable

**Decision 3: Authentication Security**
- [ ] Update `SecurityConfig.java` BCrypt to 12 rounds
- [ ] Update `DataInitializer.java` for env var admin password
- [ ] Add `app.admin.initial-password` to `application.yml`
- [ ] Test admin password reset via environment variable

**Decision 4: CSRF Protection**
- [ ] Add CSRF meta tags to base layout template
- [ ] Create `ApiKeyFilter` for external API clients
- [ ] Update `SecurityConfig` for dual authentication
- [ ] Create `csurf-helper.js` for AJAX requests
- [ ] Add CSRF tokens to all forms
- [ ] Update AJAX calls to use `securedFetch()`
- [ ] Set `API_KEY` environment variable

**Decision 5: Service-Layer Authorization**
- [ ] Add `@EnableMethodSecurity` to `SecurityConfig`
- [ ] Add `@PreAuthorize("hasRole('ADMIN')")` to admin methods
- [ ] Add ownership checks to seller methods
- [ ] Test authorization from multiple entry points

---

## Summary

**All 5 architectural decisions have been finalized:**

1. ✅ **Secrets Management** - Environment variables with credential rotation
2. ✅ **Password Encryption** - Spring Security Crypto with rotation support
3. ✅ **Authentication Hardening** - BCrypt 12 + environment variable admin password
4. ✅ **CSRF Protection** - Dual strategy (CSRF for web, API keys for external clients)
5. ✅ **Service-Layer Authorization** - @PreAuthorize on critical methods

**Next Steps:**
- Use this document to guide implementation
- Create detailed technical specifications for each fix
- Generate user stories/epics for development team
- Schedule security fixes based on priority matrix

**Document Location:** `_bmad-output/planning-artifacts/security-architecture.md`
