# Developer Guide - Game Account Shop MVP

**Project:** Game Account Shop (fcode project)
**Your Role:** Developer (Backend, Frontend, or Full-Stack)
**Tech Stack:** Java 17, Spring Boot 3.5, MySQL 8.0, Bootstrap 5, Thymeleaf

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Project Structure](#project-structure)
3. [How to Read a User Story](#how-to-read-a-user-story)
4. [Implementation Patterns](#implementation-patterns)
5. [Naming Conventions](#naming-conventions)
6. [Code Examples](#code-examples)
7. [Testing Your Work](#testing-your-work)
8. [Common Issues](#common-issues)
9. [Asking for Help](#asking-for-help)

---

## Getting Started

### Prerequisites

**Before you start coding, make sure you have:**

1. **Java 17** installed
   - Check: `java -version` should show 17.x.x

2. **MySQL 8.0** installed and running
   - Download from: https://dev.mysql.com/downloads/mysql/

3. **An IDE** (choose one)
   - IntelliJ IDEA (recommended) - https://www.jetbrains.com/idea/
   - Eclipse - https://www.eclipse.org/
   - VS Code - https://code.visualstudio.com/

4. **Git** installed
   - Check: `git --version`

### Setup Your Development Environment

**Step 1: Get the Project Code**

```bash
# Clone or download the project
# Your Team Lead will provide the repository URL
cd game-account-shop
```

**Step 2: Create the Database**

```sql
# Open MySQL Workbench or command line
CREATE DATABASE gameaccountshop;
```

**Step 3: Configure Database Connection**

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: root
    password: YOUR_MYSQL_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**Step 4: Run the Application**

```bash
# Using Maven wrapper (included)
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

**Step 5: Verify It's Running**

Open browser: `http://localhost:8080`

---

## Project Structure

### Directory Layout

```
game-account-shop/
├── src/
│   ├── main/
│   │   ├── java/com/gameaccountshop/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # Web controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Database access
│   │   │   ├── entity/          # Database entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── enums/           # Enumerations
│   │   │   └── GameAccountShopApplication.java
│   │   └── resources/
│   │       ├── application.yml  # Configuration
│   │       ├── templates/       # Thymeleaf HTML templates
│   │       └── static/          # CSS, JS, images
│   └── test/                    # Tests
└── pom.xml                      # Maven dependencies
```

### What Each Directory Does

| Directory | Purpose | Examples |
|-----------|---------|----------|
| `config/` | Configuration classes | SecurityConfig, EmailConfig |
| `controller/` | Handle HTTP requests | AuthController, ListingController |
| `service/` | Business logic | UserService, ListingService |
| `repository/` | Database queries | UserRepository, ListingRepository |
| `entity/` | Database tables | User, GameAccount, Transaction |
| `dto/` | API request/response objects | UserDTO, ListingDTO |
| `enums/` | Fixed value sets | Role, AccountStatus |
| `templates/` | HTML pages | login.html, listings.html |
| `static/` | Static assets | style.css, script.js |

---

## How to Read a User Story

### Story Format

Every story follows this format:

```
### Story X.Y: Story Title

As a [user type],
I want [capability],
So that [value/benefit].

**Acceptance Criteria:**

**Given** [precondition]
**When** [action]
**Then** [expected outcome]
**And** [additional criteria]
```

### Example Story

```
### Story 1.2: User Registration

As a guest user,
I want to register a new account with username, password, and email,
So that I can access the platform and create listings.

**Acceptance Criteria:**

**Given** I am on the registration page
**When** I submit valid username (unique), password (min 6 chars), and email
**Then** a new user account is created
**And** the password is hashed using BCrypt (10 rounds)
**And** the user is assigned role "USER"
**And** I am redirected to the login page
**And** a success message "Đăng ký thành công!" is displayed
```

### How to Implement a Story

**Step 1: Read the entire story carefully**

Understand:
- Who is the user?
- What do they want to do?
- What should happen?

**Step 2: List all acceptance criteria**

For Story 1.2:
- ✅ Create user with valid data
- ✅ Hash password with BCrypt
- ✅ Assign "USER" role
- ✅ Redirect to login
- ✅ Show Vietnamese success message
- ✅ Handle duplicate username
- ✅ Handle short password

**Step 3: Identify what you need to create**

For Story 1.2, you need:
- `User` entity
- `UserRepository`
- `UserService` (with BCrypt)
- `AuthController` (register endpoint)
- `register.html` template
- Security configuration

**Step 4: Implement following patterns**

See "Implementation Patterns" section below.

**Step 5: Test each acceptance criterion**

Don't just test the happy path! Test error cases too.

---

## Implementation Patterns

### The Golden Rule

**Always follow this order:**

```
Browser → Controller → Service → Repository → Database
```

**NEVER skip layers!**

### Pattern 1: Create an Entity

**Entities represent database tables.**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;  // BUYER, SELLER, ADMIN

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // ... etc
}
```

### Pattern 2: Create a Repository

**Repositories handle database queries.**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring automatically implements basic CRUD

    // Add custom queries
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
```

### Pattern 3: Create a Service

**Services contain business logic.**

```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String username, String password, String email) {
        // Check if username exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // BCrypt hash
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        // Save and return
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}
```

### Pattern 4: Create a Controller

**Controllers handle HTTP requests.**

```java
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Show registration page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";  // templates/auth/register.html
    }

    // Process registration form
    @PostMapping("/register")
    public String register(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String email,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            userService.register(username, password, email);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
```

### Pattern 5: Create a Thymeleaf Template

**Templates are HTML pages with dynamic content.**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Đăng Ký - Game Account Shop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h4>Đăng Ký Tài Khoản</h4>
                    </div>
                    <div class="card-body">
                        <!-- Success Message -->
                        <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>

                        <!-- Error Message -->
                        <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>

                        <!-- Registration Form -->
                        <form th:action="@{/auth/register}" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">Tên đăng nhập</label>
                                <input type="text" class="form-control" id="username" name="username" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Mật khẩu</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">Email</label>
                                <input type="email" class="form-control" id="email" name="email">
                            </div>
                            <button type="submit" class="btn btn-primary">Đăng Ký</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

---

## Naming Conventions

### Database (MySQL)

| Type | Format | Example |
|------|--------|---------|
| Tables | `snake_case`, plural | `users`, `game_accounts` |
| Columns | `snake_case` | `user_id`, `created_at`, `seller_id` |
| Foreign Keys | `{table}_id` | `seller_id`, `buyer_id` |

### Java Code

| Type | Format | Example |
|------|--------|---------|
| Classes | `PascalCase` | `User`, `GameAccount`, `ListingController` |
| Methods | `camelCase` | `getUserById`, `createListing`, `approveListing` |
| Variables | `camelCase` | `userId`, `listingId`, `sellerName` |
| Constants | `UPPER_SNAKE_CASE` | `DEFAULT_PAGE_SIZE`, `MAX_UPLOAD_SIZE` |
| Packages | lowercase | `com.gameaccountshop.service` |

### API Endpoints

| Type | Format | Example |
|------|--------|---------|
| URL paths | Plural nouns, kebab-case | `/listings`, `/game-accounts`, `/my-listings` |
| Path variables | Singular | `/listings/{id}`, `/users/{id}` |
| Query params | camelCase | `?game=lol`, `?rank=gold` |

---

## Code Examples

### Example 1: Complete User Registration Flow

**Entity (User.java):**
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime createdAt;
    // getters/setters...
}
```

**Repository (UserRepository.java):**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

**Service (UserService.java):**
```java
@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
```

**Controller (AuthController.java):**
```java
@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired private UserService userService;

    @GetMapping("/register")
    public String showRegister() { return "auth/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String email,
                          RedirectAttributes redirectAttributes) {
        try {
            userService.register(username, password, email);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
```

### Example 2: Create Listing Flow

**Entity (GameAccount.java):**
```java
@Entity
@Table(name = "game_accounts")
public class GameAccount {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String gameName;
    private String accountName;
    private String rank;
    private Integer level;
    private BigDecimal price;
    private String description;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;  // PENDING, APPROVED, REJECTED, SOLD

    private Long sellerId;  // ID reference, NOT @ManyToOne
    private LocalDateTime createdAt;
    private LocalDateTime soldAt;
    // getters/setters...
}
```

**Service (ListingService.java):**
```java
@Service
public class ListingService {
    @Autowired private GameAccountRepository listingRepository;

    public GameAccount createListing(Long sellerId, String gameName,
                                     String rank, Integer level,
                                     BigDecimal price, String description) {
        GameAccount listing = new GameAccount();
        listing.setGameName(gameName);
        listing.setRank(rank);
        listing.setLevel(level);
        listing.setPrice(price);
        listing.setDescription(description);
        listing.setSellerId(sellerId);
        listing.setStatus(AccountStatus.PENDING);
        listing.setCreatedAt(LocalDateTime.now());
        return listingRepository.save(listing);
    }

    public List<GameAccount> getApprovedListings() {
        return listingRepository.findByStatus(AccountStatus.APPROVED);
    }
}
```

---

## Testing Your Work

### Manual Testing Checklist

**Before marking a story complete, test:**

1. **Happy Path:**
   - [ ] Feature works as described in acceptance criteria
   - [ ] Data is saved correctly in database
   - [ ] User sees correct messages

2. **Validation:**
   - [ ] Required fields are enforced
   - [ ] Invalid data shows error messages
   - [ ] Error messages are in Vietnamese

3. **Integration:**
   - [ ] Doesn't break existing features
   - [ ] Links and buttons work
   - [ ] Page displays correctly on mobile

4. **Database:**
   - [ ] Check MySQL Workbench to verify data
   - [ ] Confirm foreign keys are correct
   - [ ] Verify status values are correct

### How to Test

**Step 1: Start the application**
```bash
./mvnw spring-boot:run
```

**Step 2: Open browser**
```
http://localhost:8080
```

**Step 3: Test the feature**
- Fill out forms
- Click buttons
- Verify expected behavior

**Step 4: Check the database**
```sql
SELECT * FROM users;
SELECT * FROM game_accounts;
```

---

## Common Issues

### Issue: "Port 8080 is already in use"

**Solution:**
```bash
# On Windows
netstat -ano | findstr :8080
taskkill /PID [PID] /F

# On Mac/Linux
lsof -ti:8080 | xargs kill -9
```

### Issue: "Cannot connect to database"

**Solution:**
1. Check MySQL is running
2. Verify database name in application.yml
3. Check username and password
4. Create the database if it doesn't exist

### Issue: "White label error page"

**Solution:**
1. Check console for stack trace
2. Look for missing templates
3. Verify controller mappings
4. Check for null pointer exceptions

### Issue: "Thymeleaf template not found"

**Solution:**
- Templates go in `src/main/resources/templates/`
- Return the path without `.html`: `return "auth/register";`
- File must be at `templates/auth/register.html`

---

## Asking for Help

### When to Ask

**Ask immediately when:**
- You're stuck for more than 30 minutes
- You don't understand the acceptance criteria
- You found a bug in someone else's code
- You need clarification on requirements

### How to Ask

**Good way to ask:**
```
I'm working on Story X.Y: [Title]

I've completed:
- [x] Step 1
- [x] Step 2

I'm stuck on:
- Step 3: [describe what you tried]

Error message: [paste error here]

What I think might be wrong: [your thoughts]

Can someone help?
```

**Bad way to ask:**
```
It doesn't work. Help.
```

### Daily Standup Format

Every morning, share:

1. **Yesterday I completed:**
   - Story X.X: [what you did]

2. **Today I'm working on:**
   - Story Y.Y: [what you'll do]

3. **Blockers:**
   - [What's stopping you]

---

## Quick Reference

### Useful Commands

```bash
# Run the application
./mvnw spring-boot:run

# Build the project
./mvnw clean package

# Run tests
./mvnw test

# Check Java version
java -version

# Access MySQL
mysql -u root -p
```

### Default Credentials

**Admin Account:**
- Username: `admin`
- Password: `admin123`

### Important URLs

- Application: `http://localhost:8080`
- MySQL: `localhost:3306`
- Spring Documentation: `https://spring.io/guides`

---

## Remember

**You're part of a team!**

- Ask questions when stuck
- Help others when you can
- Test your work before saying "done"
- Follow the patterns consistently
- Keep code clean and readable

**Good luck! 🚀**
