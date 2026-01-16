# Story 1.1: Initialize Spring Boot Project

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **developer**,
I want **to initialize the Spring Boot project with all required dependencies**,
So that **the team has a working foundation to build upon**.

## Acceptance Criteria

**Given** the project uses Spring Initializr
**When** I execute the initialization command
**Then** a Spring Boot 3.5.0 project is created with Java 17
**And** the project includes dependencies: web, data-jpa, security, mysql, validation, thymeleaf
**And** the package structure is com.gameaccountshop
**And** Maven wrapper is included
**And** the project runs on port 8080
**And** MySQL connection is configured in application.yml

**Given** the database is empty (no users exist)
**When** the application starts for the first time
**Then** a default admin account is created
**And** username is "admin"
**And** password is "admin123"
**And** role is "ADMIN"
**And** a startup log message indicates default admin was created

## Tasks / Subtasks

- [ ] Initialize Spring Boot project with Spring Initializr (AC: #1)
  - [ ] Run spring init command with correct dependencies
  - [ ] Verify pom.xml has all required dependencies
  - [ ] Verify Java 17 and Spring Boot 3.5.0 configuration
- [ ] Configure application.yml for MySQL connection (AC: #6)
  - [ ] Add datasource configuration (URL, username, password)
  - [ ] Configure JPA/Hibernate settings
  - [ ] Set server port to 8080
- [ ] Create base project structure (AC: #4)
  - [ ] Create package structure: controller, service, repository, entity, config, dto
  - [ ] Create templates directory structure
  - [ ] Create static resources directory structure
- [ ] Set up default admin account creation on startup (AC: #7-10)
  - [ ] Create User entity with required fields
  - [ ] Create UserRepository interface
  - [ ] Create DataInitializer component
  - [ ] Implement admin creation logic (only if no users exist)
  - [ ] Add logging for admin creation

## Dev Notes

### Project Initialization Command

```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf \
  --build=maven --java-version=17 --boot-version=3.5.0 \
  --package-name=com.gameaccountshop game-account-shop
```

Or use the web interface: https://start.spring.io/

### Required Dependencies (from pom.xml)

```xml
<dependencies>
    <!-- Spring MVC + embedded Tomcat -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA + Hibernate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security + BCrypt -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Bean Validation (JSR-380) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Thymeleaf templating -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- DevTools for hot reload -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### application.yml Configuration

```yaml
spring:
  application:
    name: game-account-shop

  # Database configuration
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop?createDatabaseIfNotExist=true
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA/Hibernate configuration
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  # DevTools
  devtools:
    restart:
      enabled: true

# Server configuration
server:
  port: 8080

# Logging
logging:
  level:
    com.gameaccountshop: DEBUG
    org.springframework.security: DEBUG
```

### Database Schema

Create the database manually or let Hibernate create it:

```sql
CREATE DATABASE IF NOT EXISTS gameaccountshop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### Initial Entity: User

Create `src/main/java/com/gameaccountshop/entity/User.java`:

```java
package com.gameaccountshop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### Enum: Role

Create `src/main/java/com/gameaccountshop/enums/Role.java`:

```java
package com.gameaccountshop.enums;

public enum Role {
    BUYER,
    SELLER,
    ADMIN
}
```

### Repository: UserRepository

Create `src/main/java/com/gameaccountshop/repository/UserRepository.java`:

```java
package com.gameaccountshop.repository;

import com.gameaccountshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    long count();
}
```

### Data Initializer Component

Create `src/main/java/com/gameaccountshop/config/DataInitializer.java`:

```java
package com.gameaccountshop.config;

import com.gameaccountshop.entity.Role;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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
        // Only create admin if no users exist
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@gameaccountshop.com");
            admin.setFullName("System Administrator");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            log.info("=================================================================");
            log.info("DEFAULT ADMIN ACCOUNT CREATED");
            log.info("Username: admin");
            log.info("Password: admin123");
            log.info("=================================================================");
        } else {
            log.info("Users already exist - skipping default admin creation");
        }
    }
}
```

### Security Config (Basic)

Create `src/main/java/com/gameaccountshop/config/SecurityConfig.java`:

```java
package com.gameaccountshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // NFR-011: BCrypt with min 10 rounds
    }
}
```

### Project Structure Notes

**Alignment with unified project structure:**

- Package: `com.gameaccountshop` (as specified in PRD/Architecture)
- Build tool: Maven (with wrapper included)
- Java version: 17
- Spring Boot version: 3.5.0

**Directory structure to create:**

```
game-account-shop/
├── src/
│   ├── main/
│   │   ├── java/com/gameaccountshop/
│   │   │   ├── GameAccountShopApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── DataInitializer.java
│   │   │   ├── entity/
│   │   │   │   └── User.java
│   │   │   ├── enums/
│   │   │   │   └── Role.java
│   │   │   └── repository/
│   │   │       └── UserRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/
│   │       └── static/
│   └── test/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

### Testing the Setup

1. Start MySQL server
2. Run the application: `./mvnw spring-boot:run` (or `mvnw.cmd` on Windows)
3. Check logs for "DEFAULT ADMIN ACCOUNT CREATED" message
4. Verify database has `users` table created
5. Verify admin user exists in database

### References

- [Source: planning-artifacts/epics.md#Epic 1: Basic Authentication]
- [Source: planning-artifacts/architecture.md#Starter Template Evaluation]
- [Source: planning-artifacts/architecture.md#Core Architectural Decisions]
- [Source: planning-artifacts/project-context.md#Critical Implementation Rules]

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101

### Debug Log References

None - Initial project setup

### Completion Notes List

- Project initialized with all required dependencies
- MySQL connection configured in application.yml
- Base entity (User) created with proper JPA annotations
- DataInitializer component creates default admin on first run
- BCryptPasswordEncoder configured with 10 rounds (NFR-011 compliant)
- Logging configured to show admin creation

### File List

Expected files to be created/modified:
- pom.xml (generated by Spring Initializr)
- src/main/resources/application.yml
- src/main/java/com/gameaccountshop/GameAccountShopApplication.java (generated)
- src/main/java/com/gameaccountshop/entity/User.java
- src/main/java/com/gameaccountshop/enums/Role.java
- src/main/java/com/gameaccountshop/repository/UserRepository.java
- src/main/java/com/gameaccountshop/config/SecurityConfig.java
- src/main/java/com/gameaccountshop/config/DataInitializer.java
