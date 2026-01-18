# Story 1.3: Default Admin Account - Task Breaker Guide

**For:** Newbie Developers
**Story ID:** 1-3
**Epic:** Basic Authentication
**Estimated Time:** 1-2 hours

---

## 📋 Story Overview

**User Story:**
As a system administrator, I want a default admin account to be created when the system first starts, so that I can access admin features without manual database setup.

**What You'll Build:**
1. A DataInitializer component that runs on application startup
2. Logic to check if admin exists before creating
3. Startup log message to indicate admin creation

---

## 🎯 Acceptance Criteria Checklist

Use this to verify you've completed everything:

- [ ] Admin account created when database is empty
- [ ] Username is "admin"
- [ ] Password is "admin123" (BCrypt hashed)
- [ ] Role is "ADMIN"
- [ ] Startup log message indicates admin was created
- [ ] No duplicate admin created on subsequent restarts
- [ ] Existing users remain unchanged on restart

---

## 📦 Task Breakdown

### **PHASE 1: Create DataInitializer** (1 hour)

---

#### Task 1.1: Create DataInitializer Component

**What:** A Spring component that runs automatically when the application starts

**File:** `src/main/java/com/gameaccountshop/config/DataInitializer.java`

**Key Concepts:**
- `@Component` - Tells Spring to manage this class
- `CommandLineRunner` - Runs code after application context is loaded
- `@Order(1)` - Ensures this runs before other runners

```java
package com.gameaccountshop.config;

import com.gameaccountshop.entity.Role;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(1) // Run first, before any other CommandLineRunner
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        if (userRepository.findByUsername("admin").isEmpty()) {

            // Create default admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            // Log success message
            log.warn("=================================================================");
            log.warn("DEFAULT ADMIN ACCOUNT CREATED");
            log.warn("Username: admin");
            log.warn("Password: admin123");
            log.warn("=================================================================");
            log.warn("IMPORTANT: Please change the default admin password after first login!");
            log.warn("=================================================================");

        } else {
            log.info("Admin account already exists. Skipping creation.");
        }
    }
}
```

**Explanation:**
| Line | Purpose |
|------|---------|
| `@Component` | Register this class as a Spring bean |
| `implements CommandLineRunner` | Run `run()` method after app starts |
| `@Order(1)` | Run this first (before other initializers) |
| `findByUsername("admin").isEmpty()` | Check if admin exists |
| `passwordEncoder.encode("admin123")` | Hash password with BCrypt |
| `log.warn()` | Display visible warning in console |

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 2: Remove Manual Admin Insert from Migration** (15 minutes)

---

#### Task 2.1: Check V1 Migration File

**Why:** The migration file currently inserts the admin account manually with SQL. We should remove this since DataInitializer will handle it.

**File:** `src/main/resources/db/migration/V1__Create_Database_Tables.sql`

**Check if this exists (lines 109-110):**
```sql
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');
```

**Decision:** You have two options:

**Option A (Recommended):** Keep the SQL INSERT for redundancy
- Pro: Admin exists even if DataInitializer fails
- Pro: Database can be used directly by other apps
- Con: Slight duplication

**Option B:** Remove the SQL INSERT
- Pro: Single source of truth (DataInitializer)
- Con: No admin if DataInitializer has bugs

**For Newbie Project:** Use **Option A** (keep both). This provides a safety net.

---

### **PHASE 3: Configure application.yml** (15 minutes)

---

#### Task 3.1: Verify Logging Configuration

**File:** `src/main/resources/application.yml`

**Add or verify logging config:**

```yaml
# Application Configuration
spring:
  application:
    name: game-account-shop

# Server Configuration
server:
  port: 8080
  servlet:
    session:
      timeout: 30m

# Database Configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop?createDatabaseIfNotExist=true
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

# Flyway Configuration
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true

# Logging Configuration
logging:
  level:
    com.gameaccountshop: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

**✅ Verify:** The logging level `INFO` allows the admin creation log to appear.

---

### **PHASE 4: Testing** (30 minutes)

---

#### Task 4.1: Fresh Installation Test

**Steps:**

1. **Drop and recreate database:**
```sql
DROP DATABASE gameaccountshop;
CREATE DATABASE gameaccountshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Start application:**
```bash
cd game-account-shop
mvnw.cmd spring-boot:run
```

3. **Verify console output:**
```
=================================================================
DEFAULT ADMIN ACCOUNT CREATED
Username: admin
Password: admin123
=================================================================
```

4. **Verify in database:**
```sql
mysql -u root -p gameaccountshop
SELECT id, username, role FROM users WHERE username = 'admin';
```

**Expected output:**
```
+----+----------+--------+
| id | username | role   |
+----+----------+--------+
|  1 | admin    | ADMIN  |
+----+----------+--------+
```

---

#### Task 4.2: Restart Test (No Duplicate)

**Steps:**

1. **Stop the application** (Ctrl+C)

2. **Restart application:**
```bash
mvnw.cmd spring-boot:run
```

3. **Verify console output:**
```
Admin account already exists. Skipping creation.
```

4. **Verify no duplicate in database:**
```sql
SELECT COUNT(*) FROM users WHERE username = 'admin';
```

**Expected output:** `1` (not 2!)

---

#### Task 4.3: Login Test

**Steps:**

1. Open browser: `http://localhost:8080/login`

2. Login with:
   - Username: `admin`
   - Password: `admin123`

3. **Expected:** Successfully logged in, redirected to home page

---

### **PHASE 5: Code Review** (15 minutes)

---

#### Task 5.1: Self-Review Checklist

Before submitting, verify:

- [ ] DataInitializer uses `@Component` annotation
- [ ] Implements `CommandLineRunner`
- [ ] Uses `@Order(1)` to run first
- [ ] Checks if admin exists before creating
- [ ] Uses BCrypt password encoder (not plaintext)
- [ ] Sets role to `Role.ADMIN`
- [ ] Logs clear warning messages
- [ ] No duplicate admin on restart
- [ ] Console output shows admin creation message

---

### **PHASE 6: Understanding the Code** (Optional Reading)

---

#### How Spring Startup Works

```
Application Starts
        ↓
Spring loads @Configuration classes
        ↓
Spring creates DataSource connection
        ↓
Flyway runs migrations (creates tables, inserts SQL admin)
        ↓
Spring creates all @Component beans
        ↓
CommandLineRunner.run() executes (DataInitializer creates admin if needed)
        ↓
Application is ready to accept requests
```

#### Why `@Order(1)` Matters

If you have multiple `CommandLineRunner` beans, `@Order` controls execution sequence:

```java
@Component
@Order(1)  // Runs FIRST
public class DataInitializer implements CommandLineRunner { ... }

@Component
@Order(2)  // Runs SECOND
public class OtherInitializer implements CommandLineRunner { ... }
```

#### Why Use SLF4J Logger

```java
// Different log levels
log.error("Critical error!");    // RED - always visible
log.warn("Warning message");     // YELLOW - for warnings
log.info("Info message");        // WHITE - general info
log.debug("Debug message");      // Only in debug mode
```

For admin creation, we use `log.warn()` because:
- It's always visible (unlike debug)
- Stands out in console (yellow color)
- Indicates something important happened

---

## 📚 References

| File | Location | Purpose |
|------|----------|---------|
| DataInitializer | `config/DataInitializer.java` | Creates admin on startup |
| UserRepository | `repository/UserRepository.java` | Database access |
| SecurityConfig | `config/SecurityConfig.java` | Provides PasswordEncoder bean |
| V1 Migration | `db/migration/V1__Create_Database_Tables.sql` | Creates users table |

---

## 🆘 Quick Help

### Common Issues

**Issue: "No qualifying bean of type 'PasswordEncoder'"**

**Solution:** Make sure `SecurityConfig.java` has the `@Bean` method for `PasswordEncoder()`:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

---

**Issue: Admin is created every time I restart**

**Solution:** Check that you're calling `isEmpty()` not `isPresent()`:

```java
// WRONG - always creates new admin
if (userRepository.findByUsername("admin").isPresent()) {
    // create admin
}

// CORRECT - only creates if not exists
if (userRepository.findByUsername("admin").isEmpty()) {
    // create admin
}
```

---

**Issue: Console doesn't show the log message**

**Solution:** Check `application.yml` logging level:

```yaml
logging:
  level:
    com.gameaccountshop: INFO  # Should be INFO or DEBUG
```

If set to `WARN` or `ERROR`, the info message won't appear.

---

**Issue: "Cannot use statement types when a wrapper is provided"**

**Solution:** Make sure you're not using `@Transactional` in `CommandLineRunner`. If you need it, add `@Transactional(readOnly = true)`.

---

## ✅ Completion Checklist

Before marking story as done, verify:

- [ ] Admin account created on first startup
- [ ] Username: "admin"
- [ ] Password: "admin123" (hashed with BCrypt)
- [ ] Role: "ADMIN"
- [ ] No duplicate on restart
- [ ] Log message visible in console
- [ ] Can login with admin/admin123
- [ ] Code follows project-context.md standards
- [ ] Committed to feature branch

---

## 🎯 Bonus Challenge (Optional)

**Advanced:** Modify the DataInitializer to support environment variables:

```java
String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");
String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");
```

This allows production deployments to use custom admin credentials.

---

**Estimated Total Time:** 1-2 hours for a newbie developer

**Good luck! You've got this! 🚀**
