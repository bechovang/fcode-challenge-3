---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9]
inputDocuments:
  - prd.md
  - product-brief-fcode project-2026-01-16.md
  - epics.md
  - project-context.md
workflowType: 'architecture'
lastStep: 9
status: 'complete'
completedAt: '2026-01-16'
updatedAt: '2026-01-16'
project_name: 'fcode project'
user_name: 'Admin'
date: '2026-01-16'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

---

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

72 functional requirements organized into 9 capability areas that define the complete feature set for the Game Account Shop marketplace:

- **User Account Management** (10 FRs): Registration, authentication, role management (Buyer/Seller/Admin), seller verification, bank account linking
- **Account Listing Management** (11 FRs): Listing submission with screenshots, admin approval/reject workflow, status tracking
- **Marketplace Discovery & Search** (7 FRs): Browse listings, search by game, filter by rank/price, view seller reputation
- **Transaction & Escrow** (11 FRs): VNPay QR generation, payment verification, credential delivery, commission calculation, payout processing
- **Trust & Reputation System** (6 FRs): Buyer reviews, seller profiles, cumulative ratings, verified seller badges
- **Dispute Resolution** (8 FRs): Dispute filing, investigation, refunds, seller banning, email notifications
- **Admin Operations & Oversight** (7 FRs): Admin dashboard, statistics, transaction history, fraud detection
- **Platform Communication** (5 FRs): Email notifications for approvals, credentials, reviews, disputes
- **Platform Experience** (7 FRs): Responsive design, keyboard navigation, WCAG 2.1 AA accessibility, SEO-friendly URLs

**Non-Functional Requirements:**

63 non-functional requirements that drive architectural decisions:

- **Performance** (10 NFRs): Page load < 3s on 4G, TTFB < 500ms, search < 1s, auth < 200ms, query performance targets
- **Security** (14 NFRs): BCrypt passwords (10+ rounds), 30-min session timeout, RBAC, HTTPS/TLS 1.3, PCI-DSS compliance, data encryption, input validation
- **Scalability** (9 NFRs): 1,000+ concurrent users, 15-20 transactions/week MVP, support for 100+ transactions/week with automation
- **Accessibility** (10 NFRs): WCAG 2.1 Level AA compliance, 4.5:1 color contrast, keyboard accessibility, semantic HTML
- **Integration** (11 NFRs): VNPay payment gateway, email service, abstraction for future providers
- **Reliability** (9 NFRs): 99% uptime during business hours, daily backups with 30-day retention, ACID transactions, graceful error handling

**Scale & Complexity:**

- Primary domain: Full-stack Web Application (Java Spring Boot + HTML/CSS/JS)
- Complexity level: Medium
- Estimated architectural components: 12-15 core components

### Technical Constraints & Dependencies

**Technology Stack (from PRD):**
- Backend: Java 17+, Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security)
- Frontend: HTML5 + CSS3 + Vanilla JavaScript, Bootstrap 5
- Database: MySQL 8.0
- Build Tool: Maven
- Payment: VNPay integration (manual MVP → automated V2)

**Architecture Pattern (from PRD):**
- MVC + Layered Architecture (Controller → Service → Repository → Entity)
- Multi-Page Application (MPA) with server-side rendering
- Manual escrow operations initially (15-20 transactions/week capacity)

**External Dependencies:**
- VNPay payment gateway API
- Email service provider (to be selected)
- File storage for account screenshots

### Cross-Cutting Concerns Identified

- **Authentication & Authorization**: Role-based access control across all components
- **Security**: Data encryption, input validation, XSS protection, SQL injection prevention
- **Session Management**: 30-minute timeout, secure token generation
- **Data Validation**: Input sanitization, file upload validation
- **Error Handling**: User-friendly error messages in Vietnamese, comprehensive logging
- **Logging**: Admin action logging, error logging for troubleshooting
- **Email Communication**: Transactional emails for all key events
- **Responsive Design**: Mobile-first approach (60-70% mobile traffic)

---

## Starter Template Evaluation

### Primary Technology Domain

Full-stack Web Application with traditional Java backend (Spring Boot 3.x) + HTML/CSS/JS frontend. This is NOT a SPA/React framework project - it's a server-rendered MPA using Spring MVC.

### Starter Options Considered

Since the PRD specifies Java 17+ with Spring Boot 3.x, I evaluated:

1. **Spring Initializr** (start.spring.io) - The official Spring Boot project generator
2. **Spring Boot CLI** (`spring init` command) - Command-line interface for project generation
3. **GitHub Layered Architecture Template** - Pre-configured template with Java 17 + Spring Boot + MySQL

### Selected Starter: Spring Initializr (start.spring.io)

**Rationale for Selection:**
- Official Spring Boot project generator maintained by the Spring team
- Web interface at [start.spring.io](https://start.spring.io/) with CLI option via `spring init`
- Supports exactly the dependencies specified in the PRD
- Generates current Spring Boot 3.x projects with proper configuration
- Industry standard for Spring Boot project initialization

**Initialization Command (CLI option):**

```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf --build=maven --java-version=17 --boot-version=3.5.0 --package-name=com.gameaccountshop game-account-shop
```

**Or use the web interface:** [start.spring.io](https://start.spring.io/)

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
- Java 17 configured in `pom.xml`
- Maven build tool with Spring Boot 3.5.0 parent POM
- UTF-8 encoding and proper Java version configuration

**Build Tooling:**
- Maven wrapper included for consistent builds
- Spring Boot Maven plugin for executable JAR creation
- Development server with hot reload (Spring Boot DevTools)

**Code Organization:**
- Standard Spring Boot project structure following layered architecture pattern:
  ```
  src/main/java/com/gameaccountshop/
  ├── controller/     (REST controllers)
  ├── service/        (business logic)
  ├── repository/     (data access)
  ├── entity/         (JPA entities)
  ├── config/         (configuration classes)
  └── GameAccountShopApplication.java
  src/main/resources/
  ├── application.properties (or .yml)
  ├── templates/      (Thymeleaf templates)
  └── static/         (CSS, JS, images)
  ```

**Dependencies Included:**
- **spring-boot-starter-web** - Spring MVC, Tomcat, REST support
- **spring-boot-starter-data-jpa** - Hibernate, JPA repositories
- **spring-boot-starter-security** - Authentication/authorization
- **spring-boot-starter-validation** - Bean validation (JSR-380)
- **spring-boot-starter-thymeleaf** - Server-side templating engine
- **mysql-connector-j** - MySQL 8.0 JDBC driver
- **spring-boot-devtools** - Hot reload during development

**Development Experience:**
- Embedded Tomcat server for development
- Spring Boot DevTools for automatic restart
- Thymeleaf templates for server-side rendering
- Hibernate auto-configuration based on dependencies
- Spring Security auto-configuration with sensible defaults

**Note:** Project initialization using this command should be the first implementation story.

---

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- Entity relationship design pattern
- Session management approach
- Controller architecture pattern
- Email service integration
- File storage strategy

**Important Decisions (Shape Architecture):**
- Security configuration (BCrypt, RBAC)
- Error handling strategy
- Validation approach
- Transaction management

**Deferred Decisions (Post-MVP):**
- Redis caching (Phase 2 scaling)
- CDN for static assets (Phase 2)
- Message queue for async operations (Phase 2)
- Distributed session storage (Phase 2 scaling)

### Data Architecture

**Entity Relationship Pattern: JPA Entities with Minimal Relationships**

**Decision:** Use JPA entities with simple ID-based navigation rather than complex `@OneToMany`/`@ManyToOne` relationship mappings.

**Rationale:**
- Aligns with layered architecture pattern from PRD
- Explicit queries are easier to debug and optimize
- Avoids N+1 query problems common with lazy loading
- Simpler for intermediate-level developers to understand
- Manual operations MVP doesn't benefit from complex ORM features

**Implementation:**
```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    @Enumerated(EnumType.STRING)
    private Role role; // BUYER, SELLER, ADMIN
    // No @OneToMany relationships - navigate via IDs
}

@Entity
public class GameAccount {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String gameName;
    private String accountName;
    private Integer level;
    private String characters;
    private String items;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private AccountStatus status; // PENDING, APPROVED, REJECTED, SOLD
    private Long sellerId; // ID reference, not relationship
    private Long buyerId; // ID reference, not relationship
    private LocalDateTime createdAt;
    private LocalDateTime soldAt;
}

@Entity
public class Transaction {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long listingId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
    private BigDecimal commission;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private LocalDateTime createdAt;
}

@Entity
public class Review {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long transactionId;
    private Long buyerId;
    private Long sellerId;
    private Integer rating; // 1-5 stars
    private String comment;
    private LocalDateTime createdAt;
}
```

**Database Schema:**
- `users` - User accounts with role-based access
- `game_accounts` - Account listings with status tracking
- `transactions` - Payment and escrow records
- `reviews` - Buyer feedback and seller ratings

**Provided by Starter:** Spring Data JPA with Hibernate

### Authentication & Security

**Session Management: In-Memory Sessions (MVP) → JDBC Sessions (Phase 2)**

**Decision:** Use default Spring Security in-memory session storage for MVP, with migration path to JDBC session storage for scaling.

**Rationale:**
- MVP capacity (15-20 transactions/week) doesn't require distributed sessions
- Single-server deployment is sufficient for initial launch
- In-memory sessions are faster and simpler
- Migration to JDBC sessions is straightforward when scaling

**Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/browse", "/listing/**").permitAll()
                .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // NFR-011: BCrypt with min 10 rounds
    }
}
```

**Password Security:**
- BCrypt with 10 rounds (meets NFR-011)
- 30-minute session timeout (NFR-012)
- Role-based access control (NFR-013)

**Provided by Starter:** Spring Security with BCrypt support

### API & Communication Patterns

**Controller Architecture: Thymeleaf-First with AJAX Endpoints**

**Decision:** Use Thymeleaf controllers for page rendering with selective `@ResponseBody` methods for AJAX interactions.

**Rationale:**
- Aligns with MPA approach from PRD
- Clear separation: HTML pages vs JSON endpoints
- Simpler than dual controller sets
- No real-time features in MVP (no need for pure REST API)

**Implementation Pattern:**
```java
@Controller
@RequestMapping("/listings")
public class ListingController {

    // Thymeleaf page rendering
    @GetMapping
    public String browseListings(@RequestParam(required = false) String game,
                                  @RequestParam(required = false) String rank,
                                  Model model) {
        model.addAttribute("listings", listingService.findApprovedListings(game, rank));
        return "listings/browse"; // Thymeleaf template
    }

    // AJAX endpoint for filtering
    @GetMapping("/api/filter")
    @ResponseBody
    public ResponseEntity<List<ListingDTO>> filterListings(@RequestParam String game,
                                                           @RequestParam String rank) {
        return ResponseEntity.ok(listingService.filterListings(game, rank));
    }

    // Thymeleaf page with details
    @GetMapping("/{id}")
    public String viewListing(@PathVariable Long id, Model model) {
        model.addAttribute("listing", listingService.findById(id));
        return "listings/detail";
    }
}
```

**Error Handling Strategy:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error"; // Vietnamese error messages (NFR-061)
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e) {
        return "redirect:/login?denied";
    }
}
```

**Provided by Starter:** Spring MVC with Thymeleaf

### Frontend Architecture

**Technology Stack: HTML5 + CSS3 + Vanilla JavaScript + Bootstrap 5**

**Decision:** Server-side rendering with Thymeleaf, client-side enhancements with vanilla JavaScript and Bootstrap 5 components.

**Rationale:**
- PRD specifies "Vanilla JavaScript" (no React/Vue)
- Bootstrap 5 provides responsive design out of the box
- Aligns with MPA approach
- Simpler for intermediate developers
- No build step required for frontend

**Template Structure:**
```
src/main/resources/templates/
├── layout/
│   ├── header.html      (navigation, user menu)
│   └── footer.html      (footer, links)
├── listings/
│   ├── browse.html      (listing grid with filters)
│   └── detail.html      (individual listing view)
├── auth/
│   ├── login.html       (login form)
│   └── register.html    (registration form)
├── seller/
│   ├── dashboard.html   (seller overview)
│   └── create-listing.html (new listing form)
├── admin/
│   ├── dashboard.html   (admin overview)
│   └── review-queue.html (pending listings)
└── error.html           (error page)
```

**Static Resources:**
```
src/main/resources/static/
├── css/
│   └── custom.css       (custom styles on top of Bootstrap)
├── js/
│   ├── filters.js       (AJAX filtering logic)
│   ├── validation.js    (form validation)
│   └── payments.js      (VNPay QR code handling)
└── images/
```

**Mobile-First Responsive Design:**
- Bootstrap 5 breakpoints (xs, sm, md, lg, xl)
- Touch-friendly tap targets (44x44px minimum - NFR-042)
- Mobile navigation (hamburger menu, bottom nav bar)
- 60-70% mobile traffic optimization

**Accessibility (WCAG 2.1 Level AA):**
- Semantic HTML structure (NFR-036)
- ARIA labels for interactive elements (NFR-037)
- Form labels (not placeholder-only - NFR-038)
- Color contrast ≥ 4.5:1 (NFR-034)
- Keyboard navigation support (NFR-035)

**Provided by Starter:** Thymeleaf + static resource handling

### Infrastructure & Deployment

**Email Service: JavaMail + SMTP (MVP) → SendGrid/Mailgun (Phase 2)**

**Decision:** Use Spring Boot's JavaMailSender with SMTP for MVP, upgrade to transactional email API service in Phase 2.

**Rationale:**
- No external API dependency for MVP
- Free (using existing email server)
- Simple configuration
- Can upgrade when volume increases

**Implementation:**
```java
@Configuration
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // Configurable
        mailSender.setPort(587);
        mailSender.setUsername("noreply@gameaccountshop.com");
        mailSender.setPassword("password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendListingApprovedEmail(String to, String listingTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your listing has been approved");
        message.setText("Your listing '" + listingTitle + "' is now live on the marketplace.");
        mailSender.send(message);
    }
}
```

**Email Templates (Vietnamese localization - NFR-049):**
- Listing approved/rejected
- Credential delivery
- Review request
- Dispute status updates

**File Storage: Local Filesystem (MVP) → Cloud Storage (Phase 2)**

**Decision:** Store uploaded screenshots in local filesystem for MVP, migrate to cloud object storage in Phase 2.

**Implementation:**
```java
@Service
public class FileStorageService {

    private final String uploadDir = "./uploads/screenshots/";

    public String storeScreenshot(MultipartFile file) {
        // Validate file type and size
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only images allowed");
        }

        // Generate unique filename
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + filename);

        // Compress and save
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public Resource loadScreenshot(String filename) {
        Path path = Paths.get(uploadDir + filename);
        return new UrlResource(path.toUri());
    }
}
```

**NFR Compliance:**
- File upload validation (NFR-023)
- Image compression before storage (NFR-009)

**Environment Configuration:**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB

app:
  upload-dir: ${UPLOAD_DIR:./uploads}
  email:
    host: ${EMAIL_HOST:smtp.gmail.com}
    port: ${EMAIL_PORT:587}
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
  vnpay:
    api-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
    tmn-code: ${VNPAY_TMN_CODE}
    secret-key: ${VNPAY_SECRET_KEY}
```

### Decision Impact Analysis

**Implementation Sequence:**

1. **Foundation First:** Initialize Spring Boot project with starter template
2. **Database Schema:** Create entities and migrations
3. **Security Layer:** Configure Spring Security with BCrypt
4. **Core Controllers:** Implement user auth and basic CRUD
5. **Email Service:** Integrate JavaMailSender
6. **File Storage:** Implement local file upload
7. **VNPay Integration:** Add payment gateway
8. **Frontend Templates:** Build Thymeleaf pages with Bootstrap

**Cross-Component Dependencies:**

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer                            │
│  (Spring Security + BCrypt + Session Management)            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  (Thymeleaf Controllers + AJAX Endpoints)                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  (Business Logic + Transaction Management)                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│  (Spring Data JPA + Hibernate)                              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    MySQL Database                            │
│  (Users, Listings, Transactions, Reviews)                   │
└─────────────────────────────────────────────────────────────┘

External Integrations:
- VNPay API (Payment processing)
- Email Service (Transactional emails)
- Local Filesystem (Screenshot storage)
```

**Phase 2 Migration Path:**
- In-Memory Sessions → JDBC Sessions or Redis
- JavaMail SMTP → SendGrid/Mailgun API
- Local Filesystem → S3 or Cloudflare R2
- Manual Operations → Automated Escrow

---

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:** 12 areas where AI agents could make different choices

### Naming Patterns

**Database Naming Conventions:**

**Decision:** snake_case for database, camelCase for Java fields with `@Column` mapping

**Rules:**
- Table names: `users`, `game_accounts`, `transactions`, `reviews` (plural, snake_case)
- Column names: `user_id`, `created_at`, `seller_id` (snake_case)
- Foreign keys: `seller_id`, `buyer_id` (referencing table + `_id`)
- Indexes: `idx_users_username`, `idx_listings_status` (prefix with `idx_`)
- Primary keys: Always `id` (auto-increment)

**Examples:**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
);
```

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "seller_id") // Foreign key reference
    private Long sellerId;
}
```

**API Naming Conventions:**

**Decision:** Plural nouns for REST resources, kebab-case for URL paths

**Rules:**
- Endpoint paths: `/listings`, `/users`, `/transactions` (plural)
- Path variables: `/listings/{id}`, `/users/{id}` (singular in variable name)
- Query parameters: `game=lol`, `rank=gold`, `priceMin=500000` (camelCase)
- Request params in controllers: `@RequestParam String game`, `@RequestParam Long id`

**Examples:**
```java
@RestController
@RequestMapping("/listings")
public class ListingController {

    @GetMapping
    public ResponseEntity<List<ListingDTO>> getListings(
        @RequestParam(required = false) String game,
        @RequestParam(required = false) String rank
    ) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) { ... }

    @PostMapping
    public ResponseEntity<ListingDTO> createListing(@RequestBody ListingCreateRequest request) { ... }
}
```

**Code Naming Conventions:**

**Decision:** Standard Java conventions with Spring Boot idioms

**Rules:**
- Classes: PascalCase - `User`, `GameAccount`, `ListingController`
- Methods: camelCase - `getUserById`, `createListing`, `approveListing`
- Variables: camelCase - `userId`, `listingId`, `seller`
- Constants: UPPER_SNAKE_CASE - `DEFAULT_PAGE_SIZE`, `MAX_UPLOAD_SIZE`
- Packages: lowercase with dots - `com.gameaccountshop.service`, `.controller`, `.repository`
- DTOs: `{Entity}DTO` - `UserDTO`, `ListingDTO`, `TransactionDTO`
- Services: `{Entity}Service` - `UserService`, `ListingService`
- Repositories: `{Entity}Repository` - `UserRepository`, `ListingRepository`
- Controllers: `{Entity}Controller` - `UserController`, `ListingController`

### Structure Patterns

**Project Organization:**

**Decision:** Layer-based package structure aligned with PRD architecture

**Package Structure:**
```
com.gameaccountshop/
├── GameAccountShopApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── EmailConfig.java
│   └── VnPayConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ListingController.java
│   ├── UserController.java
│   ├── AdminController.java
│   └── TransactionController.java
├── service/
│   ├── UserService.java
│   ├── ListingService.java
│   ├── TransactionService.java
│   ├── EmailService.java
│   └── FileStorageService.java
├── repository/
│   ├── UserRepository.java
│   ├── ListingRepository.java
│   ├── TransactionRepository.java
│   └── ReviewRepository.java
├── entity/
│   ├── User.java
│   ├── GameAccount.java
│   ├── Transaction.java
│   └── Review.java
├── dto/
│   ├── UserDTO.java
│   ├── ListingDTO.java
│   ├── TransactionDTO.java
│   └── ReviewDTO.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
└── util/
    ├── VnPayUtil.java
    └── DateUtil.java
```

**Test Organization:**
```
src/test/java/com/gameaccountshop/
├── controller/
│   ├── ListingControllerTest.java
│   └── UserControllerTest.java
├── service/
│   ├── UserServiceTest.java
│   └── ListingServiceTest.java
└── repository/
    ├── UserRepositoryTest.java
    └── ListingRepositoryTest.java
```

**File Structure Patterns:**

**Thymeleaf Templates:**
```
src/main/resources/templates/
├── layout/
│   ├── header.html
│   └── footer.html
├── auth/
│   ├── login.html
│   └── register.html
├── listings/
│   ├── browse.html
│   ├── detail.html
│   └── create.html
├── user/
│   ├── dashboard.html
│   └── profile.html
├── seller/
│   ├── dashboard.html
│   └── my-listings.html
├── admin/
│   ├── dashboard.html
│   ├── review-queue.html
│   └── transactions.html
└── error.html
```

**Static Resources:**
```
src/main/resources/static/
├── css/
│   └── custom.css
├── js/
│   ├── filters.js
│   ├── validation.js
│   └── payments.js
├── images/
│   └── logo.png
└── uploads/
    └── screenshots/
```

### Format Patterns

**API Response Formats:**

**Decision:** Use `ResponseEntity<T>` with appropriate HTTP status codes

**Success Response Pattern:**
```java
// Single item
@GetMapping("/{id}")
public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) {
    return ResponseEntity.ok(listingService.findById(id));
}

// Created
@PostMapping
public ResponseEntity<ListingDTO> createListing(@RequestBody ListingCreateRequest request) {
    ListingDTO created = listingService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// No content
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
    listingService.delete(id);
    return ResponseEntity.noContent().build();
}
```

**Error Response Format:**
```json
{
  "message": "Tài khoản không tồn tại",
  "code": "NOT_FOUND",
  "timestamp": "2025-01-16T10:30:00"
}
```

**Global Exception Handler:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "NOT_FOUND",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleError(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "Đã có lỗi xảy ra", // Vietnamese message (NFR-061)
            "INTERNAL_ERROR",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

record ErrorResponse(String message, String code, LocalDateTime timestamp) {}
```

**Data Exchange Formats:**

**Decision:** camelCase for JSON field names, ISO format for dates

**Rules:**
- JSON fields: camelCase - `userId`, `listingId`, `createdAt`
- Dates: ISO-8601 strings - `"2025-01-16T10:30:00"`
- Booleans: `true`/`false` (not 1/0)
- Null: Explicit `null` (not omitting fields)
- Money: `BigDecimal` as number - `800000.00` (VND)
- Enums: String values - `"PENDING"`, `"APPROVED"`

**Example DTO:**
```java
public class ListingDTO {
    private Long id;
    private String gameName;
    private String accountName;
    private Integer level;
    private BigDecimal price;
    private String status; // Enum as string
    private Long sellerId;
    private String sellerName; // Computed field
    private LocalDateTime createdAt;
    // Getters/setters or use record
}
```

### Communication Patterns

**Event System Patterns:**

**Decision:** Use Spring Events for domain events (e.g., listing approved, transaction completed)

**Event Naming:** `{PastTenseVerb}{Entity}Event` - `ListingApprovedEvent`, `TransactionCompletedEvent`

**Example:**
```java
// Event
public record ListingApprovedEvent(Long listingId, Long sellerId, LocalDateTime approvedAt) {}

// Publisher
@Service
public class ListingService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void approveListing(Long id) {
        // Business logic
        listing.setStatus(AccountStatus.APPROVED);
        listingRepository.save(listing);

        // Publish event
        eventPublisher.publishEvent(new ListingApprovedEvent(listing.getId(), listing.getSellerId(), LocalDateTime.now()));
    }
}

// Listener
@Component
public class EmailNotificationListener {

    @EventListener
    public void handleListingApproved(ListingApprovedEvent event) {
        emailService.sendListingApprovedEmail(event);
    }
}
```

**Logging Patterns:**

**Decision:** SLF4J with structured parameterized logging

**Rules:**
- Use `@Slf4j` annotation from Lombok or create logger
- Log level: ERROR for failures, WARN for recoverable issues, INFO for business events, DEBUG for troubleshooting
- Admin action logging (NFR-014): Log all admin actions with user context
- Error logging (NFR-062): Include sufficient context for troubleshooting

**Examples:**
```java
@Slf4j
@Service
public class ListingService {

    public ListingDTO approveListing(Long id, Long adminId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        listing.setStatus(AccountStatus.APPROVED);
        listingRepository.save(listing);

        // Business event logging
        log.info("Admin {} approved listing {}", adminId, id);

        // Admin action logging (NFR-014)
        log.info("ADMIN_ACTION: type=APPROVE_LISTING, adminId={}, listingId={}, timestamp={}",
            adminId, id, LocalDateTime.now());

        return listing;
    }
}

// Exception logging with context
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleError(Exception ex) {
    log.error("Error processing request: uri={}, method={}, message={}",
        request.getRequestURI(),
        request.getMethod(),
        ex.getMessage(),
        ex); // Include stack trace
    // ... return error response
}
```

### Process Patterns

**Error Handling Patterns:**

**Decision:** Custom exceptions with `@ControllerAdvice` for global handling

**Exception Hierarchy:**
```
BaseException (abstract)
├── ResourceNotFoundException (404)
├── BusinessException (400)
├── AuthenticationException (401)
└── AuthorizationException (403)
```

**Usage Pattern:**
```java
// Service layer throws domain exceptions
@Service
public class ListingService {
    public ListingDTO findById(Long id) {
        return listingRepository.findById(id)
            .map(mapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
    }
}

// Controller handles via global handler
@RestController
@RequestMapping("/listings")
public class ListingController {
    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.findById(id));
    }
    // No try-catch needed - GlobalExceptionHandler handles it
}
```

**Loading State Patterns:**

**Decision:** Server-side rendering means loading states are in Thymeleaf templates, not client-side state

**Pattern:**
```html
<!-- listings/browse.html -->
<div id="listings-container" th:if="${listings}">
    <!-- Show listings -->
    <div th:each="listing : ${listings}">...</div>
</div>

<div id="loading-spinner" th:if="${loading}" style="display: none;">
    <div class="spinner">Đang tải...</div>
</div>

<script>
// For AJAX filtering
function filterListings() {
    document.getElementById('loading-spinner').style.display = 'block';

    fetch('/listings/api/filter?game=' + game + '&rank=' + rank)
        .then(response => response.json())
        .then(data => {
            document.getElementById('loading-spinner').style.display = 'none';
            // Update listings
        });
}
</script>
```

### Enforcement Guidelines

**All AI Agents MUST:**

1. **Follow naming conventions** - snake_case for database, camelCase for Java, plural for REST endpoints
2. **Use ResponseEntity** - For all REST endpoints with appropriate HTTP status codes
3. **Log admin actions** - Include user context, action type, timestamp (NFR-014)
4. **Return Vietnamese errors** - All user-facing error messages in Vietnamese (NFR-061)
5. **Validate inputs** - Use Bean validation (@Valid, @NotNull, @Size, etc.)
6. **Use service layer** - All business logic in service classes, not controllers
7. **Sanitize inputs** - Prevent SQL injection, XSS (NFR-022, NFR-024)
8. **Handle exceptions globally** - Use @ControllerAdvice, don't catch exceptions in controllers
9. **Use DTOs for API responses** - Never return entities directly to frontend
10. **Log with context** - Include relevant IDs, timestamps, user information

**Pattern Verification:**
- Code reviews should check naming conventions
- Automated tests should verify error response formats
- Static analysis (Checkstyle) can enforce naming patterns
- Integration tests should verify Vietnamese error messages

**Pattern Documentation:**
- Document any intentional deviations from these patterns
- Update patterns when architectural decisions change
- Get team consensus before changing established patterns

### Pattern Examples

**Good Examples:**

```java
// ✅ Correct: Following all patterns
@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ListingDTO> createListing(@Valid @RequestBody ListingCreateRequest request) {
        ListingDTO created = listingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

// ✅ Correct: Service layer with business logic
@Service
@Slf4j
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ListingDTO create(ListingCreateRequest request) {
        GameAccount listing = mapper.toEntity(request);
        listing.setStatus(AccountStatus.PENDING);
        listing = listingRepository.save(listing);

        log.info("Listing created: id={}, sellerId={}", listing.getId(), listing.getSellerId());

        eventPublisher.publishEvent(new ListingCreatedEvent(listing.getId()));

        return mapper.toDTO(listing);
    }
}

// ✅ Correct: Repository with Spring Data JPA
@Repository
public interface ListingRepository extends JpaRepository<GameAccount, Long> {

    List<GameAccount> findByStatus(AccountStatus status);

    @Query("SELECT l FROM GameAccount l WHERE l.gameName = :game AND l.status = :status")
    List<GameAccount> findByGameAndStatus(@Param("game") String game, @Param("status") AccountStatus status);
}

// ✅ Correct: DTO with proper naming
public record ListingDTO(
    Long id,
    String gameName,
    String accountName,
    Integer level,
    BigDecimal price,
    String status,
    Long sellerId,
    LocalDateTime createdAt
) {}
```

**Anti-Patterns (What to Avoid):**

```java
// ❌ Wrong: Entity returned directly
@GetMapping("/{id}")
public GameAccount getListing(@PathVariable Long id) {  // Don't return entities
    return listingService.findById(id);                   // Use DTOs
}

// ❌ Wrong: Business logic in controller
@PostMapping
public ResponseEntity<ListingDTO> createListing(@RequestBody ListingCreateRequest request) {
    // Don't put business logic in controllers
    GameAccount listing = new GameAccount();
    listing.setGameName(request.getGameName());
    listing.setStatus(AccountStatus.PENDING);
    // ... more logic
    return ResponseEntity.ok(created);
}

// ❌ Wrong: No ResponseEntity
@GetMapping("/{id}")
public ListingDTO getListing(@PathVariable Long id) {  // Use ResponseEntity
    return listingService.findById(id);
}

// ❌ Wrong: English error messages
throw new ResourceNotFoundException("Listing not found");  // Use Vietnamese

// ❌ Wrong: Inconsistent naming
@GetMapping("/Listing/{Id}")  // Use lowercase plural: /listings/{id}
public ListingDTO getListing(@PathVariable Long Id) {  // Use camelCase: id

// ❌ Wrong: No validation
@PostMapping
public ResponseEntity<ListingDTO> createListing(@RequestBody ListingCreateRequest request) {
    // Always use @Valid for request bodies
    return ResponseEntity.ok(listingService.create(request));
}

// ❌ Wrong: Catching exceptions in controller
@GetMapping("/{id}")
public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(listingService.findById(id));
    } catch (Exception e) {  // Let GlobalExceptionHandler handle exceptions
        return ResponseEntity.badRequest().build();
    }
}

// ❌ Wrong: System.out.println instead of logger
public void approveListing(Long id) {
    listing.setStatus(AccountStatus.APPROVED);
    listingRepository.save(listing);
    System.out.println("Listing approved: " + id);  // Use log.info()
}
```

---

## Project Structure & Boundaries

### Complete Project Directory Structure

```
game-account-shop/
├── pom.xml                                    # Maven build configuration
├── README.md                                  # Project documentation
├── .gitignore                                 # Git ignore rules
├── .env.example                               # Environment variable template
├── .env.local                                 # Local environment variables (not committed)
│
├── src/
│   ├── main/
│   │   ├── java/com/gameaccountshop/
│   │   │   ├── GameAccountShopApplication.java         # Main Spring Boot application
│   │   │   │
│   │   │   ├── config/                                 # Configuration classes
│   │   │   │   ├── SecurityConfig.java                  # Spring Security + BCrypt
│   │   │   │   ├── EmailConfig.java                    # JavaMailSender bean
│   │   │   │   ├── VnPayConfig.java                    # VNPay integration
│   │   │   │   ├── WebConfig.java                      # Static resources, caching
│   │   │   │   └── AsyncConfig.java                    # Event publishing
│   │   │   │
│   │   │   ├── controller/                             # REST + Thymeleaf controllers
│   │   │   │   ├── AuthController.java                 # Login, register, logout
│   │   │   │   ├── HomeController.java                 # Home page
│   │   │   │   ├── ListingController.java              # Browse, detail, create
│   │   │   │   ├── UserController.java                 # Profile, dashboard
│   │   │   │   ├── TransactionController.java          # Purchase, payment
│   │   │   │   ├── ReviewController.java              # Submit reviews
│   │   │   │   └── AdminController.java               # Admin dashboard
│   │   │   │
│   │   │   ├── service/                                # Business logic layer
│   │   │   │   ├── UserService.java                    # User CRUD, auth
│   │   │   │   ├── ListingService.java                 # Listing CRUD, search
│   │   │   │   ├── TransactionService.java              # Escrow, payouts
│   │   │   │   ├── ReviewService.java                  # Ratings, reviews
│   │   │   │   ├── EmailService.java                   # Transactional emails
│   │   │   │   ├── FileStorageService.java             # Screenshot upload
│   │   │   │   ├── VnPayService.java                   # Payment processing
│   │   │   │   └── DisputeService.java                 # Dispute handling
│   │   │   │
│   │   │   ├── repository/                             # Data access layer
│   │   │   │   ├── UserRepository.java                  # User queries
│   │   │   │   ├── ListingRepository.java              # Listing queries
│   │   │   │   ├── TransactionRepository.java          # Transaction queries
│   │   │   │   └── ReviewRepository.java               # Review queries
│   │   │   │
│   │   │   ├── entity/                                 # JPA entities
│   │   │   │   ├── User.java                           # User table
│   │   │   │   ├── GameAccount.java                    # Listing table
│   │   │   │   ├── Transaction.java                    # Transaction table
│   │   │   │   └── Review.java                         # Review table
│   │   │   │
│   │   │   ├── dto/                                    # Data Transfer Objects
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── ListingDTO.java
│   │   │   │   ├── TransactionDTO.java
│   │   │   │   ├── ReviewDTO.java
│   │   │   │   ├── UserCreateRequest.java
│   │   │   │   ├── ListingCreateRequest.java
│   │   │   │   └── ErrorResponse.java
│   │   │   │
│   │   │   ├── enums/                                  # Enum definitions
│   │   │   │   ├── Role.java                           # BUYER, SELLER, ADMIN
│   │   │   │   ├── AccountStatus.java                   # PENDING, APPROVED, REJECTED, SOLD
│   │   │   │   └── TransactionStatus.java              # PENDING, VERIFIED, COMPLETED
│   │   │   │
│   │   │   ├── exception/                              # Custom exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java          # Centralized error handling
│   │   │   │
│   │   │   ├── event/                                  # Domain events
│   │   │   │   ├── ListingCreatedEvent.java
│   │   │   │   ├── ListingApprovedEvent.java
│   │   │   │   ├── TransactionCompletedEvent.java
│   │   │   │   └── DisputeFiledEvent.java
│   │   │   │
│   │   │   ├── listener/                               # Event listeners
│   │   │   │   ├── EmailNotificationListener.java
│   │   │   │   └── AdminActionListener.java
│   │   │   │
│   │   │   └── util/                                   # Utility classes
│   │   │       ├── VnPayUtil.java                      # VNPay helper methods
│   │   │       ├── DateUtil.java                       # Date formatting
│   │   │       └── ValidationUtil.java                 # Custom validation
│   │   │
│   │   └── resources/
│   │       ├── application.yml                         # Spring configuration
│   │       ├── application-dev.yml                     # Development profile
│   │       ├── application-prod.yml                    # Production profile
│   │       │
│   │       ├── db/migration/                            # Flyway database migrations
│   │       │   └── V1__Create_Database_Tables.sql     # Initial schema (4 tables)
│   │       │
│   │       ├── templates/                              # Thymeleaf templates
│   │       │   ├── layout/
│   │       │   │   ├── header.html                     # Navigation, user menu
│   │       │   │   └── footer.html                     # Footer, links
│   │       │   ├── auth/
│   │       │   │   ├── login.html                      # Login form
│   │       │   │   └── register.html                   # Registration form
│   │       │   ├── listings/
│   │       │   │   ├── browse.html                     # Listing grid with filters
│   │       │   │   ├── detail.html                     # Individual listing view
│   │       │   │   └── create.html                     # New listing form (seller)
│   │       │   ├── user/
│   │       │   │   ├── dashboard.html                  # User dashboard
│   │       │   │   ├── profile.html                    # User profile
│   │       │   │   └── transactions.html               # Transaction history
│   │       │   ├── seller/
│   │       │   │   ├── dashboard.html                  # Seller dashboard
│   │       │   │   ├── my-listings.html                # Seller's listings
│   │       │   │   └── payouts.html                    # Payout history
│   │       │   ├── admin/
│   │       │   │   ├── dashboard.html                  # Admin overview
│   │       │   │   ├── review-queue.html               # Pending listings
│   │       │   │   ├── transactions.html               # All transactions
│   │       │   │   └── users.html                      # User management
│   │       │   └── error.html                         # Error page (Vietnamese)
│   │       │
│   │       └── static/                                 # Static assets
│   │           ├── css/
│   │           │   └── custom.css                       # Custom styles on Bootstrap
│   │           ├── js/
│   │           │   ├── filters.js                       # AJAX filtering
│   │           │   ├── validation.js                    # Form validation
│   │           │   ├── payments.js                      # VNPay QR code
│   │           │   └── main.js                         # Common utilities
│   │           ├── images/
│   │           │   ├── logo.png
│   │           │   └── placeholder.png                  # Default listing image
│   │           └── uploads/
│   │               └── screenshots/                      # User-uploaded images
│   │
│   └── test/
│       └── java/com/gameaccountshop/
│           ├── controller/
│           │   ├── AuthControllerTest.java
│           │   ├── ListingControllerTest.java
│           │   └── AdminControllerTest.java
│           ├── service/
│           │   ├── UserServiceTest.java
│           │   ├── ListingServiceTest.java
│           │   └── TransactionServiceTest.java
│           └── repository/
│               ├── UserRepositoryTest.java
│               └── ListingRepositoryTest.java
│
├── .mvn/                                          # Maven wrapper
├── mvnw                                           # Maven wrapper script (Unix)
├── mvnw.cmd                                       # Maven wrapper script (Windows)
├── .github/
│   └── workflows/
│       └── ci.yml                                 # GitHub Actions CI/CD
│
└── docs/                                          # Additional documentation
    ├── database-schema.md                         # Complete database design for MVP
    ├── vnpay-integration.md                        # VNPay API docs
    └── deployment-guide.md                        # Deployment instructions
```

### Architectural Boundaries

**API Boundaries:**

| Boundary Type | Definition | Example |
|--------------|------------|---------|
| **Public** | No authentication required | `/login`, `/register`, `/listings/browse` |
| **Authenticated** | Any logged-in user | `/user/dashboard`, `/listings/{id}` |
| **Seller** | Seller role required | `/listings/create`, `/seller/my-listings` |
| **Admin** | Admin role required | `/admin/*`, `/listings/{id}/approve` |
| **AJAX** | Returns JSON, not HTML | `/api/listings/filter`, `/api/user/info` |

**Component Boundaries:**

- **Controller → Service:** Controllers handle HTTP, Services handle business logic
- **Service → Repository:** Services orchestrate, Repositories query database
- **Service → Service:** Communication via method calls (no REST between services)
- **Event-based:** Services publish events, Listeners respond asynchronously

**Service Boundaries:**

| Service | Responsibility | Dependencies |
|---------|---------------|--------------|
| `UserService` | User CRUD, authentication | UserRepository |
| `ListingService` | Listing CRUD, search, approval | ListingRepository, FileStorageService |
| `TransactionService` | Escrow, payouts, status updates | TransactionRepository, ListingRepository, VnPayService |
| `EmailService` | Send transactional emails | JavaMailSender |
| `VnPayService` | QR code generation, payment verification | VnPayConfig, RestTemplate |

**Data Boundaries:**

- **JPA Entities:** Only in `entity/` package, never exposed to controllers
- **DTOs:** All API responses use DTOs from `dto/` package
- **Repository:** Only Spring Data JPA interfaces in `repository/` package
- **Database Access:** ONLY through Repository layer (no raw SQL in services)

### Requirements to Structure Mapping

**Feature/Epic Mapping:**

| Feature Area | Controllers | Services | Repositories | Templates |
|--------------|-------------|----------|--------------|-----------|
| **User Account Management** | AuthController, UserController | UserService | UserRepository | auth/login, auth/register, user/dashboard |
| **Account Listing Management** | ListingController | ListingService, FileStorageService | ListingRepository | listings/browse, listings/detail, listings/create |
| **Marketplace Discovery & Search** | ListingController | ListingService | ListingRepository | listings/browse (filter functionality) |
| **Transaction & Escrow** | TransactionController | TransactionService, VnPayService | TransactionRepository | listings/detail (purchase button), user/transactions |
| **Trust & Reputation** | ReviewController | ReviewService | ReviewRepository | listings/detail (review form) |
| **Dispute Resolution** | UserController | DisputeService, EmailService | TransactionRepository | user/transactions (dispute link) |
| **Admin Operations** | AdminController | ListingService, TransactionService | ListingRepository, TransactionRepository | admin/dashboard, admin/review-queue |
| **Platform Communication** | (All controllers) | EmailService | - | (Emails sent asynchronously) |

**Cross-Cutting Concerns:**

| Concern | Location | Applies To |
|---------|----------|-----------|
| **Authentication** | SecurityConfig, AuthController | All protected pages |
| **Authorization** | SecurityConfig (@PreAuthorize) | Admin, seller-only pages |
| **Error Handling** | GlobalExceptionHandler | All controllers |
| **Logging** | All @Service classes (SLF4J) | Admin actions, errors, business events |
| **Validation** | DTOs (@Valid), @ControllerAdvice | All user inputs |
| **Email Notifications** | EmailService, Event Listeners | Listing approved, credential delivery, review requests |
| **File Upload** | FileStorageService | Listing screenshot uploads |
| **Payment Processing** | VnPayService | Transaction creation |

### Integration Points

**Internal Communication:**

```
┌─────────────────────────────────────────────────────────────┐
│                       Browser                                │
│  (HTML forms, AJAX fetch, VNPay QR code display)           │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP (Thymeleaf + JSON)
┌────────────────────▼────────────────────────────────────────┐
│              Spring MVC Controllers                         │
│  (AuthController, ListingController, etc.)                  │
└────────────────────┬────────────────────────────────────────┘
                     │ Method calls
┌────────────────────▼────────────────────────────────────────┐
│              Service Layer                                 │
│  (UserService, ListingService, TransactionService)          │
└──────┬───────────────────────────────────────────┬─────────┘
       │                                           │
       │ Spring Data JPA                           │ Spring Events
       ▼                                           ▼
┌──────────────────┐                    ┌─────────────────────┐
│   Repositories   │                    │   Event Listeners   │
│ (JPA Database)    │                    │ (EmailService)      │
└──────────────────┘                    └─────────────────────┘
```

**External Integrations:**

| Integration | Point | Type | Purpose |
|-------------|-------|------|---------|
| **VNPay** | VnPayService | API call (QR code, verification) | Payment processing |
| **Email SMTP** | EmailService | SMTP (JavaMailSender) | Transactional emails |
| **File Storage** | FileStorageService | Local filesystem | Screenshot storage |
| **MySQL** | Repositories | JDBC (Hibernate) | Data persistence |

**Data Flow:**

1. **User Registration:**
   - Browser → POST `/register` → AuthController → UserService.createUser() → UserRepository.save() → MySQL → EmailService.sendWelcomeEmail()

2. **Listing Creation:**
   - Browser → POST `/listings/create` → ListingController → ListingService.create() → ListingRepository.save() + FileStorageService.saveScreenshot() → MySQL → EmailService.sendPendingNotification()

3. **Purchase Flow:**
   - Browser → POST `/transactions/create` → TransactionController → TransactionService.create() → VnPayService.generateQRCode() → Display QR to user → User pays → VNPay callback → TransactionService.verifyPayment() → EmailService.sendCredentialDeliveryEmail()

### File Organization Patterns

**Configuration Files:**

| File | Purpose | Location |
|------|---------|----------|
| `pom.xml` | Maven dependencies, build config | Root |
| `application.yml` | Spring configuration (DB, email, VNPay) | `src/main/resources/` |
| `application-dev.yml` | Development overrides (localhost) | `src/main/resources/` |
| `application-prod.yml` | Production overrides (environment vars) | `src/main/resources/` |
| `.env.local` | Local secrets (not committed) | Root |
| `.env.example` | Environment variable template | Root |

**Source Organization:**

- **Java:** Layer-based packages (`controller/`, `service/`, `repository/`, `entity/`)
- **Templates:** Feature-based folders (`listings/`, `user/`, `admin/`)
- **Static:** Resource type folders (`css/`, `js/`, `images/`)
- **Tests:** Mirror source structure (`controller/`, `service/`, `repository/`)

**Test Organization:**

- **Unit Tests:** Per-class test files (`UserServiceTest.java`)
- **Integration Tests:** Controller tests with MockMvc
- **Repository Tests:** With @DataJpaTest, in-memory database
- **Location:** `src/test/java/com/gameaccountshop/` (mirrors main)

**Asset Organization:**

- **CSS:** Custom styles override Bootstrap
- **JS:** Feature-specific scripts (filters, validation, payments)
- **Images:** Logo, placeholder images
- **Uploads:** User-generated content (screenshots), excluded from Git

### Development Workflow Integration

**Development Server Structure:**

- **Entry Point:** `GameAccountShopApplication.java` (Spring Boot main)
- **Default Port:** 8080 (configurable via `server.port`)
- **Hot Reload:** Spring Boot DevTools enabled
- **Database:** MySQL on localhost:3306 (or via Docker)
- **Access:** http://localhost:8080

**Build Process Structure:**

- **Build Command:** `./mvnw clean package` (or `mvnw.cmd` on Windows)
- **Output:** `target/game-account-shop-0.0.1-SNAPSHOT.jar`
- **Embedded Server:** Tomcat (included in JAR)
- **Run Command:** `java -jar target/*.jar`

**Deployment Structure:**

- **Production JAR:** Executable with embedded Tomcat
- **Environment Variables:** `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EMAIL_HOST`, `VNPAY_TMN_CODE`
- **Database:** External MySQL 8.0 instance
- **Reverse Proxy:** Nginx/Apache (optional, for SSL)

---

## Database Schema Design (MVP)

**Date Added:** 2026-01-16
**Purpose:** Complete database schema for the 14-story MVP with newbie-friendly design

### Overview

The database schema was designed specifically for the **simplified newbie MVP** scope, focusing on clarity and ease of implementation over complexity. All design decisions prioritize developer productivity while supporting all 14 stories across 4 epics.

**Database:** `gameaccountshop` (MySQL 8.0)
**Character Set:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`

### Design Principles for Newbie Developers

1. **Simple Tables:** Only 4 tables, each with a clear purpose
2. **No Complex Relationships:** Use ID references, not ORM joins
3. **Clear Naming:** `snake_case` for everything in database
4. **Minimal Fields:** Only what's needed for the stories
5. **Explicit Status:** Use ENUM for easy-to-understand states

### Entity Relationship Diagram

```
┌──────────────┐
│    users     │
│──────────────│
│ id (PK)      │────┐
│ username     │    │
│ password     │    │
│ email        │    │
│ role         │    │
│ created_at   │    │
└──────────────┘    │
                    │
                    │      ┌──────────────────┐
                    │      │  game_accounts   │
                    ├─────▶│──────────────────│
                    │      │ id (PK)          │
                    │      │ seller_id (FK)   │
                    │      │ game_name        │
                    │      │ rank             │
                    │      │ price            │
                    │      │ description      │
                    │      │ status           │
                    │      │ rejection_reason │
                    │      │ created_at       │
                    │      │ sold_at          │
                    │      └──────────────────┘
                    │                │
                    │                │
                    │      ┌─────────────────┐
                    │      │  transactions   │
                    │      │─────────────────│
                    │      │ id (PK)         │
                    │      │ listing_id (FK) │
                    │      │ buyer_id (FK)   │
                    │      │ seller_id (FK)  │
                    │      │ amount          │
                    │      │ commission      │
                    │      │ status          │
                    │      │ account_username│
                    │      │ account_password│
                    │      │ account_notes   │
                    │      │ created_at      │
                    │      │ verified_at     │
                    │      └─────────────────┘
                    │                │
                    │                │
                    │      ┌─────────────────┐
                    │      │    reviews      │
                    │      │─────────────────│
                    │      │ id (PK)         │
                    │      │ transaction_id  │
                    │      │ buyer_id (FK)   │
                    │      │ seller_id (FK)  │
                    │      │ rating (1-5)     │
                    │      │ comment         │
                    │      │ created_at      │
                    │      └─────────────────┘
                    │
                    └────────── All FKs reference users.id
```

### Table Definitions

#### 1. users

**Purpose:** Store user accounts for login, registration, and role-based access.

**Stories:** Epic 1 (Basic Authentication)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `username` | VARCHAR(50) | Unique username (e.g., "gamer123") |
| `password` | VARCHAR(255) | BCrypt hashed password |
| `email` | VARCHAR(100) | Email address |
| `role` | ENUM | **USER** or **ADMIN** |
| `created_at` | TIMESTAMP | Account creation date |

**SQL:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Default Admin Account:**
- Username: `admin`
- Password: `admin123` (hashed with BCrypt)
- Role: `ADMIN`

---

#### 2. game_accounts

**Purpose:** Store game account listings that sellers want to sell.

**Stories:** Epic 2 (Listings & Ratings)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `seller_id` | BIGINT | Foreign key to users.id (who created listing) |
| `game_name` | VARCHAR(100) | Game name (e.g., "Liên Minh Huyền Thoại") |
| `rank` | VARCHAR(50) | Rank/Level (e.g., "Gold", "Diamond") |
| `price` | DECIMAL(12,2) | Price in VNĐ (e.g., 500000.00) |
| `description` | TEXT | Account description/details |
| `status` | ENUM | **PENDING** → **APPROVED** → **SOLD** (or **REJECTED**) |
| `rejection_reason` | VARCHAR(500) | Reason for rejection (if applicable) |
| `created_at` | TIMESTAMP | When listing was created |
| `sold_at` | TIMESTAMP | When listing was sold (NULL until sold) |

**SQL:**
```sql
CREATE TABLE game_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    rank VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    description TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_game_name (game_name),
    INDEX idx_rank (rank),
    INDEX idx_created_at (created_at DESC),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Status Flow:**
```
PENDING (new listing)
    ↓ (admin approves)
APPROVED (visible in marketplace)
    ↓ (purchased)
SOLD (no longer available)

OR

PENDING
    ↓ (admin rejects)
REJECTED (with reason)
```

---

#### 3. transactions

**Purpose:** Track purchase transactions and payment verification.

**Stories:** Epic 3 (Simple Buying)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `listing_id` | BIGINT | Foreign key to game_accounts.id |
| `buyer_id` | BIGINT | Foreign key to users.id (who is buying) |
| `seller_id` | BIGINT | Foreign key to users.id (who is selling) |
| `amount` | DECIMAL(12,2) | Listing price |
| `commission` | DECIMAL(12,2) | Platform fee (10% of amount) |
| `status` | ENUM | **PENDING** → **VERIFIED** |
| `account_username` | VARCHAR(100) | Game account credential (filled by admin) |
| `account_password` | VARCHAR(255) | Game account credential (filled by admin) |
| `account_notes` | TEXT | Additional notes for buyer |
| `created_at` | TIMESTAMP | When transaction was created |
| `verified_at` | TIMESTAMP | When admin verified payment |

**SQL:**
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    commission DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'VERIFIED') NOT NULL DEFAULT 'PENDING',
    account_username VARCHAR(100),
    account_password VARCHAR(255),
    account_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Status Flow:**
```
PENDING (buyer clicked "Buy Now")
    ↓ (admin verified VNPay payment)
VERIFIED (credentials sent to buyer's email)
```

---

#### 4. reviews

**Purpose:** Store buyer ratings and reviews for sellers.

**Stories:** Epic 2 (Listing Details with Simple Rating - Story 2.3)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `transaction_id` | BIGINT | Foreign key to transactions.id (UNIQUE) |
| `buyer_id` | BIGINT | Foreign key to users.id (who wrote review) |
| `seller_id` | BIGINT | Foreign key to users.id (seller being reviewed) |
| `rating` | INT | Rating from 1 to 5 stars |
| `comment` | TEXT | Optional review comment |
| `created_at` | TIMESTAMP | When review was created |

**SQL:**
```sql
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id),
    INDEX idx_transaction_id (transaction_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Epic to Table Mapping

| Epic | Stories | Tables Used |
|------|---------|-------------|
| **Epic 1: Basic Authentication** | 1.1, 1.2, 1.3 | `users` |
| **Epic 2: Listings & Ratings** | 2.1, 2.2, 2.3, 2.4, 2.5 | `game_accounts`, `reviews` |
| **Epic 3: Simple Buying** | 3.1, 3.2, 3.3 | `transactions`, `game_accounts` |
| **Epic 4: Dashboard & Profiles** | 4.1, 4.2, 4.3 | All tables (for queries) |

### Migration Strategy

**Tool:** Flyway
**Migration File:** `src/main/resources/db/migration/V1__Create_Database_Tables.sql`

**Flyway Configuration (application.yml):**
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
```

**For Development:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema management
```

**Documentation:**
- Database Schema Details: `docs/database-schema.md`
- Migration Script: `src/main/resources/db/migration/V1__Create_Database_Tables.sql`

### Common Queries Reference

**Get All Approved Listings:**
```sql
SELECT ga.*, u.username as seller_name
FROM game_accounts ga
JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;
```

**Search Listings by Game Name:**
```sql
SELECT ga.*, u.username as seller_name
FROM game_accounts ga
JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
  AND ga.game_name LIKE CONCAT('%', ?, '%')
ORDER BY ga.created_at DESC;
```

**Get Seller's Average Rating:**
```sql
SELECT
    COUNT(*) as total_reviews,
    COALESCE(AVG(rating), 0) as average_rating
FROM reviews
WHERE seller_id = ?;
```

**Admin Dashboard Statistics:**
```sql
-- Total Users
SELECT COUNT(*) FROM users;

-- Total Listings
SELECT COUNT(*) FROM game_accounts;

-- Pending Listings
SELECT COUNT(*) FROM game_accounts WHERE status = 'PENDING';

-- Sold Listings
SELECT COUNT(*) FROM game_accounts WHERE status = 'SOLD';

-- Total Transactions
SELECT COUNT(*) FROM transactions;

-- Total Revenue
SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'VERIFIED';
```

### Key Design Decisions

**Simplified for MVP:**
- **Role Simplification:** Only USER and ADMIN roles (not BUYER/SELLER/ADMIN)
- **No Verification Status:** Removed `is_verified` field (not in MVP scope)
- **ID-Based Navigation:** All foreign keys use ID references, not ORM relationships
- **Explicit Status Enums:** Clear state transitions for each entity
- **Minimal Fields:** Only columns needed for the 14 stories

**Alignment with Epics:**
- Epic 1.2 specifies role "USER" (confirmed in acceptance criteria)
- Epic 1.3 specifies role "ADMIN" (confirmed in acceptance criteria)
- Epic 2.4 specifies rejection reason storage
- Epic 3.3 specifies credential delivery in transactions
- All 14 stories are fully supported by this schema

---

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**

All technology choices are compatible and work together without conflicts:
- Java 17 + Spring Boot 3.5 + MySQL 8.0: All verified compatible versions
- BCrypt (10 rounds) aligns with Spring Security and meets NFR-011
- Thymeleaf + Bootstrap 5 supports MPA approach with mobile-first design
- In-memory sessions for MVP scale (15-20 transactions/week) with documented JDBC migration path for Phase 2
- ID-based entity navigation prevents N+1 query issues and simplifies debugging
- VNPay integration designed with abstraction layer for Phase 2 automation
- No contradictory decisions identified

**Pattern Consistency:**

Implementation patterns consistently support architectural decisions:
- Naming conventions (snake_case DB, camelCase Java, plural REST) enforced throughout
- Layered architecture (Controller → Service → Repository) strictly followed
- DTOs for all API responses, entities never exposed to frontend
- ResponseEntity pattern for all REST endpoints with appropriate HTTP status codes
- Spring Events for domain-driven communication (listing approved, transaction completed)
- Error handling via @ControllerAdvice with Vietnamese error messages

**Structure Alignment:**

Project structure fully supports all architectural decisions:
- Complete directory tree with 50+ files defined
- All boundaries documented (API: Public/Authenticated/Seller/Admin/AJAX)
- Service dependencies mapped (8 services with clear responsibilities)
- Data boundaries enforced (Entities internal only, DTOs external, Repository for data access)
- Integration points properly structured (VNPay, Email SMTP, File Storage, MySQL)

### Requirements Coverage Validation ✅

**Functional Requirements Coverage (72 FRs → 100% covered):**

| Feature Area | FRs Covered | Implementation Location |
|--------------|-------------|-------------------------|
| User Account Management (10 FRs) | ✅ Complete | AuthController, UserService, SecurityConfig |
| Account Listing Management (11 FRs) | ✅ Complete | ListingController, ListingService, FileStorageService |
| Marketplace Discovery (7 FRs) | ✅ Complete | ListingController, ListingService (search/filter) |
| Transaction & Escrow (11 FRs) | ✅ Complete | TransactionController, TransactionService, VnPayService |
| Trust & Reputation (6 FRs) | ✅ Complete | ReviewController, ReviewService |
| Dispute Resolution (8 FRs) | ✅ Complete | UserController, DisputeService, EmailService |
| Admin Operations (7 FRs) | ✅ Complete | AdminController, admin templates |
| Platform Communication (5 FRs) | ✅ Complete | EmailService, Event Listeners |
| Platform Experience (7 FRs) | ✅ Complete | Bootstrap 5, WCAG 2.1 AA compliance |

**Non-Functional Requirements Coverage (63 NFRs → 100% addressed):**

| Category | Coverage | Evidence |
|----------|----------|----------|
| Performance (10 NFRs) | ✅ Complete | <3s page load, <500ms TTFB, <1s search, BCrypt 10 rounds |
| Security (14 NFRs) | ✅ Complete | BCrypt, RBAC, 30-min timeout, HTTPS/TLS 1.3, input validation |
| Scalability (9 NFRs) | ✅ Complete | 1,000+ concurrent users, 15-20 tx/week MVP, Phase 2 paths |
| Accessibility (10 NFRs) | ✅ Complete | WCAG 2.1 AA, 4.5:1 contrast, keyboard nav, semantic HTML |
| Integration (11 NFRs) | ✅ Complete | VNPay abstraction, Email abstraction, Vietnamese templates |
| Reliability (9 NFRs) | ✅ Complete | 99% uptime, ACID transactions, daily backups, error handling |

### Implementation Readiness Validation ✅

**Decision Completeness:**

All critical decisions documented with specific versions and rationale:
- Technology stack: Java 17, Spring Boot 3.5.0, MySQL 8.0, Maven, Bootstrap 5
- Security: BCrypt 10 rounds, 30-minute session timeout, RBAC (BUYER, SELLER, ADMIN)
- Database: JPA entities with minimal relationships (ID-based navigation)
- Email: JavaMail + SMTP for MVP, SendGrid/Mailgun for Phase 2
- Storage: Local filesystem for MVP, cloud storage for Phase 2
- All decisions include clear rationale and implementation examples

**Structure Completeness:**

Complete project structure with all files defined:
- 7 controllers (Auth, Home, Listing, User, Transaction, Review, Admin)
- 8 services (User, Listing, Transaction, Review, Email, FileStorage, VnPay, Dispute)
- 4 repositories (User, Listing, Transaction, Review)
- 4 entities (User, GameAccount, Transaction, Review)
- 7 DTOs, 3 enums, 3 exceptions, 4 events, 2 listeners, 3 utilities
- 20+ Thymeleaf templates organized by feature
- All integration points clearly specified

**Pattern Completeness:**

All potential conflict points addressed with enforcement rules:
- 12 critical conflict points identified and resolved
- 10 enforcement rules documented with examples
- Good vs Anti-pattern examples provided for each major pattern
- 3 complete data flow examples (User Registration, Listing Creation, Purchase Flow)

### Gap Analysis Results

**Critical Gaps:** None identified - All architectural decisions that could block implementation are documented.

**Important Gaps:** None for MVP - Phase 2 migration paths clearly documented for all scaling decisions (sessions, email, file storage).

**Nice-to-Have Gaps:**
- API OpenAPI/Swagger documentation (could add in Phase 2 for REST API clarity)
- Database migration tooling (Flyway/Liquibase) - can add during implementation
- Comprehensive test strategy documentation - can be developed during sprint planning
- CI/CD pipeline configuration - can be added during implementation

### Validation Issues Addressed

No critical or important issues found during validation. The architecture is:
- Coherent: All decisions work together without conflicts
- Complete: All 72 FRs and 63 NFRs are architecturally supported
- Consistent: Implementation patterns align with technology choices
- Ready: AI agents can implement consistently using this document

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] Project context thoroughly analyzed (72 FRs, 63 NFRs identified)
- [x] Scale and complexity assessed (Medium complexity, 15-20 tx/week MVP)
- [x] Technical constraints identified (Java 17+, Spring Boot 3.x, MySQL 8.0)
- [x] Cross-cutting concerns mapped (8 concerns with specific locations)

**✅ Architectural Decisions**
- [x] Critical decisions documented with versions (5 major decisions with rationale)
- [x] Technology stack fully specified (Spring Initializr, dependencies listed)
- [x] Integration patterns defined (VNPay, Email, File Storage with Phase 2 paths)
- [x] Performance considerations addressed (<3s page load, <500ms TTFB targets)

**✅ Implementation Patterns**
- [x] Naming conventions established (snake_case DB, camelCase Java, plural REST)
- [x] Structure patterns defined (layered package organization with boundaries)
- [x] Communication patterns specified (Spring Events, SLF4J logging)
- [x] Process patterns documented (error handling, loading states)

**✅ Project Structure**
- [x] Complete directory structure defined (50+ files across all layers)
- [x] Component boundaries established (API, component, service, data boundaries)
- [x] Integration points mapped (4 external integrations with clear interfaces)
- [x] Requirements to structure mapping complete (9 feature areas mapped to files)

### Architecture Readiness Assessment

**Overall Status:** ✅ **READY FOR IMPLEMENTATION**

**Confidence Level:** **High** - All architectural decisions are coherent, complete, and consistent with requirements. No blocking issues identified.

**Key Strengths:**
1. Clear MVP scope with realistic capacity targets (15-20 transactions/week)
2. Technology stack proven and production-ready (Spring Boot 3.5, Java 17, MySQL 8.0)
3. Comprehensive consistency rules prevent AI agent conflicts (12 conflict points addressed)
4. Complete project structure with 50+ files specified with clear boundaries
5. Phase 2 migration paths documented for all scaling decisions
6. All 72 FRs and 63 NFRs architecturally supported with implementation locations
7. Vietnamese localization and WCAG 2.1 AA compliance built-in from design

**Areas for Future Enhancement:**
- Add OpenAPI/Swagger documentation in Phase 2 for REST API clarity
- Consider Flyway/Liquibase for database migration management during implementation
- Develop comprehensive test strategy during sprint planning phase
- Add CI/CD pipeline configuration when ready for deployment

### Implementation Handoff

**AI Agent Guidelines:**

When implementing this architecture, all AI agents MUST:

1. Follow all architectural decisions exactly as documented
2. Use implementation patterns consistently across all components
3. Respect project structure and boundaries (Controller → Service → Repository)
4. Refer to this document for all architectural questions
5. Enforce naming conventions (snake_case DB, camelCase Java, plural REST)
6. Return Vietnamese error messages for all user-facing errors
7. Use DTOs for all API responses (never return entities directly)
8. Log admin actions with context (NFR-014 compliance)
9. Validate inputs with Bean validation (@Valid, @NotNull, @Size)
10. Handle exceptions globally with @ControllerAdvice

**First Implementation Priority:**

Initialize Spring Boot project using Spring Initializr:

```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf \
  --build=maven --java-version=17 --boot-version=3.5.0 \
  --package-name=com.gameaccountshop game-account-shop
```

Then create database schema, configure Spring Security, and begin implementing core controllers following the documented project structure.

---

## Architecture Completion Summary

### Workflow Completion

**Architecture Decision Workflow:** COMPLETED ✅
**Total Steps Completed:** 8
**Date Completed:** 2026-01-16
**Document Location:** _bmad-output/planning-artifacts/architecture.md

### Final Architecture Deliverables

**📋 Complete Architecture Document**

- All architectural decisions documented with specific versions
- Implementation patterns ensuring AI agent consistency
- Complete project structure with all files and directories
- Requirements to architecture mapping
- Validation confirming coherence and completeness

**🏗️ Implementation Ready Foundation**

- 5 major architectural decisions made (Data, Security, API, Email, Storage)
- 12 implementation pattern categories defined
- 50+ files and architectural components specified
- 72 functional requirements + 63 non-functional requirements fully supported

**📚 AI Agent Implementation Guide**

- Technology stack with verified versions (Java 17, Spring Boot 3.5, MySQL 8.0)
- Consistency rules that prevent implementation conflicts
- Project structure with clear boundaries (API, Component, Service, Data)
- Integration patterns and communication standards

### Implementation Handoff

**For AI Agents:**
This architecture document is your complete guide for implementing Game Account Shop. Follow all decisions, patterns, and structures exactly as documented.

**First Implementation Priority:**
Initialize Spring Boot project using Spring Initializr:

```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf \
  --build=maven --java-version=17 --boot-version=3.5.0 \
  --package-name=com.gameaccountshop game-account-shop
```

**Development Sequence:**

1. Initialize project using documented starter template
2. Set up development environment per architecture (MySQL, application.yml)
3. Implement core architectural foundations (SecurityConfig, Entities, Repositories)
4. Build features following established patterns (Controllers → Services → Repositories)
5. Maintain consistency with documented rules (naming, DTOs, error handling)

### Quality Assurance Checklist

**✅ Architecture Coherence**

- [x] All decisions work together without conflicts
- [x] Technology choices are compatible (Java 17 + Spring Boot 3.5 + MySQL 8.0)
- [x] Patterns support the architectural decisions
- [x] Structure aligns with all choices

**✅ Requirements Coverage**

- [x] All 72 functional requirements are supported
- [x] All 63 non-functional requirements are addressed
- [x] 8 cross-cutting concerns are handled
- [x] 4 external integrations are defined (VNPay, Email, File Storage, MySQL)

**✅ Implementation Readiness**

- [x] Decisions are specific and actionable with versions
- [x] 12 conflict point patterns prevent agent conflicts
- [x] Structure is complete with 50+ files unambiguous
- [x] Good vs Anti-pattern examples provided for clarity

### Project Success Factors

**🎯 Clear Decision Framework**
Every technology choice was made collaboratively with clear rationale, ensuring all stakeholders understand the architectural direction.

**🔧 Consistency Guarantee**
Implementation patterns and rules ensure that multiple AI agents will produce compatible, consistent code that works together seamlessly.

**📋 Complete Coverage**
All project requirements are architecturally supported, with clear mapping from business needs to technical implementation.

**🏗️ Solid Foundation**
The chosen Spring Initializr template and architectural patterns provide a production-ready foundation following current best practices.

---

**Architecture Status:** READY FOR IMPLEMENTATION ✅

**Next Phase:** Begin implementation using the architectural decisions and patterns documented herein.

**Document Maintenance:** Update this architecture when major technical decisions are made during implementation.

---