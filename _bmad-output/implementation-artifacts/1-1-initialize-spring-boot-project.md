# Story 1.1: Initialize Spring Boot Project

Status: done

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

- [x] Initialize Spring Boot project with Spring Initializr (AC: #1)
  - [x] Run spring init command with correct dependencies
  - [x] Verify pom.xml has all required dependencies
  - [x] Verify Java 17 and Spring Boot 3.5.0 configuration
- [x] Configure application.yml for MySQL connection (AC: #6)
  - [x] Add datasource configuration (URL, username, password)
  - [x] Configure JPA/Hibernate settings
  - [x] Set server port to 8080
- [x] Create base project structure (AC: #4)
  - [x] Create package structure: controller, service, repository, entity, config, dto
  - [x] Create templates directory structure
  - [x] Create static resources directory structure
- [x] Set up default admin account creation on startup (AC: #7-10)
  - [x] Create User entity with required fields
  - [x] Create UserRepository interface
  - [x] Create DataInitializer component
  - [x] Implement admin creation logic (only if no users exist)
  - [x] Add logging for admin creation

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
      ddl-auto: validate  # Flyway manages schema
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  # Flyway configuration
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

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
    org.springframework.security: INFO
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

import com.gameaccountshop.enums.Role;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

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
    USER,
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

import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void run(String... args) throws Exception {
        // Only create admin if no users exist
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@gameaccountshop.com");
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // NFR-011: BCrypt with min 10 rounds
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // TODO: Configure proper security in Epic 1 stories
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for MVP development
        return http.build();
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

glm-4.6

### Debug Log References

- Fixed Role import: Changed `com.gameaccountshop.entity.Role` to `com.gameaccountshop.enums.Role` in DataInitializer.java

### Completion Notes List

- Project initialized with all required dependencies (web, data-jpa, security, mysql, validation, thymeleaf, flyway)
- MySQL connection configured in application.yml with Flyway migrations enabled
- Base entity (User) created with proper JPA annotations aligned with database schema
- Role enum uses USER and ADMIN (simplified for MVP, aligned with database schema)
- DataInitializer component creates default admin on first run
- BCryptPasswordEncoder configured with 10 rounds (NFR-011 compliant)
- Logging configured to show admin creation on startup
- Project compiled successfully with `mvn compile`
- JPA ddl-auto set to 'validate' (Flyway manages schema)
- .gitignore file created for Maven/IDE files
- Maven wrapper files generated (mvnw, mvnw.cmd)
- SecurityFilterChain configured to permit all requests during MVP development
- @Transactional annotation added to DataInitializer for atomic admin creation
- Story Dev Notes updated to match actual implementation

### File List

Files created/modified:
- game-account-shop/pom.xml
- game-account-shop/.gitignore
- game-account-shop/mvnw (Maven wrapper script)
- game-account-shop/mvnw.cmd (Maven wrapper script for Windows)
- game-account-shop/.mvn/wrapper/maven-wrapper.jar
- game-account-shop/.mvn/wrapper/maven-wrapper.properties
- game-account-shop/src/main/resources/application.yml
- game-account-shop/src/main/resources/db/migration/V1__Create_Database_Tables.sql
- game-account-shop/src/main/java/com/gameaccountshop/GameAccountShopApplication.java
- game-account-shop/src/main/java/com/gameaccountshop/enums/Role.java
- game-account-shop/src/main/java/com/gameaccountshop/entity/User.java
- game-account-shop/src/main/java/com/gameaccountshop/repository/UserRepository.java
- game-account-shop/src/main/java/com/gameaccountshop/config/SecurityConfig.java
- game-account-shop/src/main/java/com/gameaccountshop/config/DataInitializer.java
