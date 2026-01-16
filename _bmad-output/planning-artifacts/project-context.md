---
project_name: 'fcode project'
user_name: 'Admin'
date: '2026-01-16'
sections_completed: ['technology_stack', 'critical_rules']
existing_patterns_found: 12
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents MUST follow when implementing code for Game Account Shop. These are unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

**Backend:**
- Java 17
- Spring Boot 3.5.0
- Spring Data JPA (Hibernate ORM)
- Spring Security (BCrypt password hashing)
- Spring Web (REST + MVC)
- Thymeleaf (server-side templating)
- MySQL 8.0
- Maven (build tool)

**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript (NO React/Vue/Angular)
- Bootstrap 5.3+ (responsive UI framework)

**Development Tools:**
- Spring Boot DevTools (hot reload)
- Maven wrapper

---

## Critical Implementation Rules

### 1. Naming Conventions (CRITICAL - MUST FOLLOW)

**Database:**
- Table names: `snake_case` plural → `users`, `game_accounts`, `transactions`, `reviews`
- Column names: `snake_case` → `user_id`, `created_at`, `seller_id`
- Foreign keys: `{table}_id` pattern → `seller_id`, `buyer_id`
- Indexes: `idx_{table}_{column}` → `idx_users_username`, `idx_listings_status`
- Primary keys: Always `id` (auto-increment BIGINT)

**Java Code:**
- Classes: `PascalCase` → `User`, `GameAccount`, `ListingController`
- Methods: `camelCase` → `getUserById`, `createListing`, `approveListing`
- Variables: `camelCase` → `userId`, `listingId`, `seller`
- Constants: `UPPER_SNAKE_CASE` → `DEFAULT_PAGE_SIZE`, `MAX_UPLOAD_SIZE`
- Packages: lowercase with dots → `com.gameaccountshop.service`, `.controller`, `.repository`
- DTOs: `{Entity}DTO` pattern → `UserDTO`, `ListingDTO`, `TransactionDTO`
- Services: `{Entity}Service` pattern → `UserService`, `ListingService`
- Repositories: `{Entity}Repository` pattern → `UserRepository`, `ListingRepository`
- Controllers: `{Entity}Controller` pattern → `UserController`, `ListingController`

**API Endpoints:**
- URL paths: Plural nouns, kebab-case → `/listings`, `/users`, `/game-accounts`
- Path variables: Singular in variable name → `/listings/{id}`, `/users/{id}`
- Query parameters: camelCase → `?game=lol`, `?rank=gold`, `?priceMin=500000`

### 2. Architecture Pattern (STRICT LAYERED)

**MUST follow this order:**
```
Browser → Controller → Service → Repository → Database
```

**Rules:**
- Controllers: ONLY handle HTTP, never business logic
- Services: ALL business logic here, never in controllers
- Repositories: ONLY data access via Spring Data JPA
- Entities: NEVER exposed to controllers (use DTOs)
- DTOs: MUST be used for all API responses

**❌ ANTI-PATTERNS TO AVOID:**
- ❌ Business logic in controllers
- ❌ Database queries in services (use repositories)
- ❌ Returning entities directly to frontend
- ❌ Raw SQL in controllers or services

### 3. Security Requirements (MANDATORY)

**Password Security:**
- BCryptPasswordEncoder with 10 rounds (minimum)
- NEVER store plaintext passwords
- Session timeout: 30 minutes of inactivity

**Role-Based Access Control:**
- Roles: `BUYER`, `SELLER`, `ADMIN`
- Use `@PreAuthorize` annotations for controller-level authorization
- Public endpoints: `/register`, `/login`, `/browse`, `/listing/**`
- Seller-only: `/seller/**` endpoints
- Admin-only: `/admin/**` endpoints

**Security Config:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10); // NFR-011: BCrypt with min 10 rounds
}
```

### 4. Error Handling (UNIFIED PATTERN)

**Global Exception Handler:**
- Use `@ControllerAdvice` for ALL exception handling
- NEVER catch exceptions in individual controllers
- Return Vietnamese error messages for all user-facing errors

**Error Response Format:**
```java
{
  "message": "Tài khoản không tồn tại",  // Vietnamese message
  "code": "NOT_FOUND",
  "timestamp": "2025-01-16T10:30:00"
}
```

**Exception Hierarchy:**
- `ResourceNotFoundException` (404)
- `BusinessException` (400)
- `AuthenticationException` (401)
- `AuthorizationException` (403)

### 5. API Response Pattern (CONSISTENT)

**All REST endpoints MUST:**
- Use `ResponseEntity<T>` wrapper
- Return appropriate HTTP status codes
- Return DTOs, NEVER entities

**Status Codes:**
- `200 OK` - Successful GET, PUT, DELETE
- `201 CREATED` - Successful POST
- `400 BAD REQUEST` - Validation errors
- `401 UNAUTHORIZED` - Not authenticated
- `403 FORBIDDEN` - Not authorized
- `404 NOT FOUND` - Resource doesn't exist
- `500 INTERNAL SERVER ERROR` - Server errors

**Example:**
```java
@GetMapping("/{id}")
public ResponseEntity<ListingDTO> getListing(@PathVariable Long id) {
    return ResponseEntity.ok(listingService.findById(id));
}

@PostMapping
public ResponseEntity<ListingDTO> createListing(@Valid @RequestBody ListingCreateRequest request) {
    ListingDTO created = listingService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

### 6. Entity Design Pattern (ID-BASED NAVIGATION)

**CRITICAL: Use ID references, NOT ORM relationships**

**✅ CORRECT:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String username;
    // NO @OneToMany relationships
}

@Entity
@Table(name = "game_accounts")
public class GameAccount {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long sellerId;  // ID reference, NOT @ManyToOne
    private Long buyerId;   // ID reference, NOT @ManyToOne
}
```

**❌ WRONG:**
```java
// Don't do this - causes N+1 queries
@ManyToOne
@JoinColumn(name = "seller_id")
private User seller;
```

**Rationale:** ID-based navigation prevents N+1 query problems and simplifies debugging.

### 7. Validation Pattern (BEAN VALIDATION)

**Request DTOs MUST:**
- Use `@Valid` annotation in controllers
- Include Bean validation annotations
- Provide clear error messages

**Validation Annotations:**
- `@NotNull` - Required fields
- `@NotBlank` - Required text fields
- `@Size(min=X, max=Y)` - String length limits
- `@Min(value)` / `@Max(value)` - Numeric limits
- `@Email` - Email format validation
- `@Pattern(regex)` - Custom patterns

**Example:**
```java
@PostMapping
public ResponseEntity<ListingDTO> createListing(@Valid @RequestBody ListingCreateRequest request) {
    // @Valid triggers validation automatically
    return ResponseEntity.ok(listingService.create(request));
}

public class ListingCreateRequest {
    @NotBlank(message = "Tên game không được để trống")
    private String gameName;

    @NotNull(message = "Giá bán là bắt buộc")
    @Positive(message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;
}
```

### 8. Logging Requirements (MANDATORY)

**Admin Actions (NFR-014):**
- ALL admin actions MUST be logged with context
- Include: admin ID, action type, target entity ID, timestamp

**Pattern:**
```java
log.info("ADMIN_ACTION: type=APPROVE_LISTING, adminId={}, listingId={}, timestamp={}",
    adminId, id, LocalDateTime.now());
```

**Error Logging:**
- Include sufficient context for troubleshooting
- Log URI, method, error message, stack trace

**Pattern:**
```java
log.error("Error processing request: uri={}, method={}, message={}",
    request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);
```

**Use SLF4J with @Slf4j annotation or explicit logger.**

### 9. Localization (VIETNAMESE REQUIRED)

**All user-facing messages MUST be in Vietnamese:**

**Error Messages:**
```java
throw new ResourceNotFoundException("Tài khoản không tồn tại");  // ✅ Vietnamese
throw new ResourceNotFoundException("Listing not found");          // ❌ English
```

**Email Templates:**
- Listing approved/rejected notifications
- Credential delivery emails
- Review request emails
- Dispute status updates

### 10. Spring Events Pattern (DOMAIN-DRIVEN)

**Event Naming:** `{PastTenseVerb}{Entity}Event`

**Example:**
```java
// Event definition
public record ListingApprovedEvent(Long listingId, Long sellerId, LocalDateTime approvedAt) {}

// Publishing
@Service
public class ListingService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void approveListing(Long id) {
        listing.setStatus(AccountStatus.APPROVED);
        listingRepository.save(listing);
        eventPublisher.publishEvent(new ListingApprovedEvent(listing.getId(), listing.getSellerId(), LocalDateTime.now()));
    }
}

// Listening
@Component
public class EmailNotificationListener {
    @EventListener
    public void handleListingApproved(ListingApprovedEvent event) {
        emailService.sendListingApprovedEmail(event);
    }
}
```

### 11. File Upload Pattern (SCREENSHOTS)

**Rules:**
- Validate file type (images only)
- Validate file size (max 5MB)
- Compress images before storage
- Generate unique filenames (UUID + original)
- Store in `./uploads/screenshots/` directory

**Configuration:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

### 12. Testing Pattern (STRUCTURE)

**Test Organization:**
- Mirror source package structure
- Unit tests: Per-class test files
- Integration tests: Controller tests with MockMvc
- Repository tests: Use @DataJpaTest with in-memory database

**Naming:**
- Test classes: `{ClassName}Test`
- Test methods: `{methodName}_{scenario}_{expectedResult}`

---

## Project-Specific Rules

### Vietnamese Market Context

**Payment Integration:**
- VNPay payment gateway (Vietnamese payment processor)
- QR code generation for mobile payments
- Manual verification workflow for MVP

**Language Support:**
- Vietnamese language interface
- Vietnamese error messages
- Vietnamese email templates

**Mobile-First Design:**
- 60-70% mobile traffic expected
- Bootstrap 5 breakpoints: xs, sm, md, lg, xl
- Touch-friendly tap targets (minimum 44x44px)

### MVP Constraints (CURRENT PHASE)

**Single Game:** Liên Minh Huyền Thoại (League of Legends) only
**Capacity:** 15-20 transactions/week with manual operations
**Email:** JavaMail + SMTP for MVP (SendGrid/Mailgun for Phase 2)
**Storage:** Local filesystem for MVP (cloud storage for Phase 2)
**Sessions:** In-memory for MVP (JDBC sessions for Phase 2)

---

## Anti-Patterns to Avoid

**❌ NEVER do these:**

1. **Entity Exposure:** Never return JPA entities to controllers
   ```java
   // ❌ WRONG
   public GameAccount getListing(Long id) { return listing; }
   // ✅ RIGHT
   public ListingDTO getListing(Long id) { return mapper.toDTO(listing); }
   ```

2. **Business Logic in Controllers:**
   ```java
   // ❌ WRONG
   @PostMapping
   public ResponseEntity<?> create(@RequestBody ListingRequest req) {
       GameAccount listing = new GameAccount();
       listing.setGameName(req.getGameName());
       listing.setStatus(AccountStatus.PENDING); // Business logic here!
       return ResponseEntity.ok(listing);
   }
   // ✅ RIGHT
   @PostMapping
   public ResponseEntity<ListingDTO> create(@RequestBody ListingRequest req) {
       return ResponseEntity.ok(listingService.create(req));
   }
   ```

3. **English Error Messages:**
   ```java
   // ❌ WRONG
   throw new ResourceNotFoundException("Listing not found");
   // ✅ RIGHT
   throw new ResourceNotFoundException("Tài khoản không tồn tại");
   ```

4. **System.out.println:**
   ```java
   // ❌ WRONG
   System.out.println("Listing approved: " + id);
   // ✅ RIGHT
   log.info("Listing approved: id={}", id);
   ```

5. **Missing Validation:**
   ```java
   // ❌ WRONG
   public ResponseEntity<ListingDTO> create(@RequestBody ListingRequest request) {
       return ResponseEntity.ok(listingService.create(request));
   }
   // ✅ RIGHT
   public ResponseEntity<ListingDTO> create(@Valid @RequestBody ListingRequest request) {
       return ResponseEntity.ok(listingService.create(request));
   }
   ```

---

## Quick Reference Commands

**Initialize Project:**
```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf \
  --build=maven --java-version=17 --boot-version=3.5.0 \
  --package-name=com.gameaccountshop game-account-shop
```

**Run Development Server:**
```bash
./mvnw spring-boot:run
```

**Build for Production:**
```bash
./mvnw clean package
java -jar target/*.jar
```

---

## Environment Variables Required

```bash
DB_URL=jdbc:mysql://localhost:3306/gameaccountshop
DB_USERNAME=root
DB_PASSWORD=password
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=noreply@gameaccountshop.com
EMAIL_PASSWORD=yourpassword
VNPAY_TMN_CODE=yourtmncode
VNPAY_SECRET_KEY=yoursecretkey
```

---

**Remember:** This file is the single source of truth for implementation patterns. When in doubt, refer to the complete architecture document at `_bmad-output/planning-artifacts/architecture.md`.
