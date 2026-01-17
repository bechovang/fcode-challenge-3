# Story 1.2: User Registration & Login - Task Breaker Guide

**For:** Newbie Developers
**Story ID:** 1-2
**Epic:** Basic Authentication
**Estimated Time:** 4-6 hours

---

## 📋 Story Overview

**User Story:**
As a guest user, I want to register a new account and log in, so that I can access the platform.

**What You'll Build:**
1. User registration page with form
2. User login page with form
3. Backend authentication logic
4. Session management
5. Logout functionality
6. Vietnamese error messages

---

## 🎯 Acceptance Criteria Checklist

Use this to verify you've completed everything:

- [ ] Registration creates user with valid data
- [ ] Password hashed with BCrypt (10 rounds)
- [ ] User assigned "USER" role
- [ ] Registration redirects to home page
- [ ] Success message: "Đăng ký thành công!"
- [ ] Error: "Tên đăng nhập đã tồn tại" (duplicate username)
- [ ] Error: "Mật khẩu phải có ít nhất 6 ký tự" (short password)
- [ ] Login authenticates successfully
- [ ] Session created after login
- [ ] Login redirects to home page
- [ ] Username displayed in navigation
- [ ] Login error: "Tên đăng nhập hoặc mật khẩu không đúng"
- [ ] Logout invalidates session
- [ ] Logout redirects to home page
- [ ] Logout message: "Đăng xuất thành công!"

---

## 📦 Task Breakdown

### **PHASE 1: Backend Foundation** (2 hours)

---

#### Task 1.1: Create Registration DTO

**What:** Create a Data Transfer Object for user registration

**File:** `src/main/java/com/gameaccountshop/dto/RegistrationRequest.java`

```java
package com.gameaccountshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

**How to test:**
```bash
# After creating, run:
cd game-account-shop
mvn compile
```

**✅ Verify:** No compilation errors

---

#### Task 1.2: Create Login DTO

**What:** Create a Data Transfer Object for user login

**File:** `src/main/java/com/gameaccountshop/dto/LoginRequest.java`

```java
package com.gameaccountshop.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

**✅ Verify:** Compile with `mvn compile`

---

#### Task 1.3: Create UserService

**What:** Business logic for user registration and authentication

**File:** `src/main/java/com/gameaccountshop/service/UserService.java`

```java
package com.gameaccountshop.service;

import com.gameaccountshop.dto.LoginRequest;
import com.gameaccountshop.dto.RegistrationRequest;
import com.gameaccountshop.entity.Role;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     * @param request Registration data
     * @return Created user
     * @throws RuntimeException if username already exists
     */
    public User register(RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setEmail(request.getEmail());
        user.setRole(Role.USER); // Default role

        return userRepository.save(user);
    }

    /**
     * Authenticate user login
     * @param request Login data
     * @return Authenticated user
     * @throws RuntimeException if credentials invalid
     */
    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        return user;
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 2: Controllers** (1.5 hours)

---

#### Task 2.1: Create AuthController

**What:** REST endpoints for registration and login

**File:** `src/main/java/com/gameaccountshop/controller/AuthController.java`

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.dto.LoginRequest;
import com.gameaccountshop.dto.RegistrationRequest;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Show registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    // Process registration
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công!");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // Process login
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            jakarta.servlet.http.HttpSession session) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            User user = userService.login(request);
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 3: Security Configuration** (30 minutes)

---

#### Task 3.1: Update SecurityConfig for Form Login

**What:** Configure Spring Security to allow public access to auth pages

**File:** `src/main/java/com/gameaccountshop/config/SecurityConfig.java`

**Replace the entire content with:**

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
                .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for MVP development
        return http.build();
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 4: Frontend Templates** (1.5 hours)

---

#### Task 4.1: Create Registration Form Template

**What:** Thymeleaf template for registration page

**File:** `src/main/resources/templates/register.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký | Game Account Shop</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">📝 Đăng Ký Tài Khoản</h4>
                    </div>
                    <div class="card-body">
                        <!-- Success Message -->
                        <div th:if="${successMessage}" class="alert alert-success alert-dismissible fade show" role="alert">
                            <span th:text="${successMessage}"></span>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>

                        <!-- Error Message -->
                        <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show" role="alert">
                            <span th:text="${errorMessage}"></span>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>

                        <!-- Registration Form -->
                        <form th:action="@{/register}" th:object="${registrationRequest}" method="post">
                            <!-- Username Field -->
                            <div class="mb-3">
                                <label for="username" class="form-label">Tên đăng nhập</label>
                                <input type="text" class="form-control"
                                       id="username"
                                       name="username"
                                       th:field="*{username}"
                                       placeholder="Nhập tên đăng nhập (3-50 ký tự)"
                                       required>
                                <div th:if="${#fields.hasErrors('username')}" class="text-danger"
                                     th:errors="*{username}">Username error</div>
                            </div>

                            <!-- Email Field -->
                            <div class="mb-3">
                                <label for="email" class="form-label">Email</label>
                                <input type="email" class="form-control"
                                       id="email"
                                       name="email"
                                       th:field="*{email}"
                                       placeholder="example@email.com"
                                       required>
                                <div th:if="${#fields.hasErrors('email')}" class="text-danger"
                                     th:errors="*{email}">Email error</div>
                            </div>

                            <!-- Password Field -->
                            <div class="mb-3">
                                <label for="password" class="form-label">Mật khẩu</label>
                                <input type="password" class="form-control"
                                       id="password"
                                       name="password"
                                       th:field="*{password}"
                                       placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)"
                                       required>
                                <div th:if="${#fields.hasErrors('password')}" class="text-danger"
                                     th:errors="*{password}">Password error</div>
                            </div>

                            <!-- Submit Button -->
                            <button type="submit" class="btn btn-primary w-100">Đăng Ký</button>

                            <!-- Login Link -->
                            <p class="text-center mt-3 mb-0">
                                Đã có tài khoản? <a th:href="@{/login}">Đăng nhập</a>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

---

#### Task 4.2: Create Login Form Template

**File:** `src/main/resources/templates/login.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | Game Account Shop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-5">
                <div class="card shadow">
                    <div class="card-header bg-success text-white">
                        <h4 class="mb-0">🔐 Đăng Nhập</h4>
                    </div>
                    <div class="card-body">
                        <!-- Success Message (after logout) -->
                        <div th:if="${param.logout}" class="alert alert-info alert-dismissible fade show" role="alert">
                            Đăng xuất thành công!
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>

                        <!-- Error Message -->
                        <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show" role="alert">
                            <span th:text="${errorMessage}"></span>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>

                        <!-- Login Form -->
                        <form th:action="@{/login}" th:object="${loginRequest}" method="post">
                            <!-- Username Field -->
                            <div class="mb-3">
                                <label for="username" class="form-label">Tên đăng nhập</label>
                                <input type="text" class="form-control"
                                       id="username"
                                       name="username"
                                       th:field="*{username}"
                                       placeholder="Nhập tên đăng nhập"
                                       required>
                                <div th:if="${#fields.hasErrors('username')}" class="text-danger"
                                     th:errors="*{username}">Username error</div>
                            </div>

                            <!-- Password Field -->
                            <div class="mb-3">
                                <label for="password" class="form-label">Mật khẩu</label>
                                <input type="password" class="form-control"
                                       id="password"
                                       name="password"
                                       th:field="*{password}"
                                       placeholder="Nhập mật khẩu"
                                       required>
                                <div th:if="${#fields.hasErrors('password')}" class="text-danger"
                                     th:errors="*{password}">Password error</div>
                            </div>

                            <!-- Submit Button -->
                            <button type="submit" class="btn btn-success w-100">Đăng Nhập</button>

                            <!-- Register Link -->
                            <p class="text-center mt-3 mb-0">
                                Chưa có tài khoản? <a th:href="@{/register}">Đăng ký</a>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

---

#### Task 4.3: Create Home Page Template

**File:** `src/main/resources/templates/index.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Game Account Shop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <!-- Navigation Bar -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" th:href="@{/}">🎮 Game Account Shop</a>

            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <!-- Logged In User -->
                    <li class="nav-item" th:if="${session.loggedInUser}">
                        <span class="navbar-text me-3">
                            Xin chào, <span th:text="${session.loggedInUser.username}">User</span>!
                        </span>
                    </li>

                    <li class="nav-item" th:if="${session.loggedInUser}">
                        <form th:action="@{/logout}" method="post" style="display: inline;">
                            <button type="submit" class="btn btn-outline-light btn-sm mt-1">Đăng xuất</button>
                        </form>
                    </li>

                    <!-- Guest User -->
                    <li class="nav-item" th:unless="${session.loggedInUser}">
                        <a class="nav-link" th:href="@{/login}">Đăng nhập</a>
                    </li>
                    <li class="nav-item" th:unless="${session.loggedInUser}">
                        <a class="nav-link" th:href="@{/register}">Đăng ký</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <div class="bg-primary text-white py-5">
        <div class="container text-center">
            <h1 class="display-4">Chợ Mua Bán Tài Khoản Game</h1>
            <p class="lead">Nền tảng mua bán tài khoản game uy tín nhất Việt Nam</p>
        </div>
    </div>

    <!-- Features Section -->
    <div class="container mt-5">
        <div class="row">
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h5>🎯 An Toàn</h5>
                        <p class="text-muted">Hệ thống xác minh tài khoản</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h5>💰 Thanh Toán An Toàn</h5>
                        <p class="text-muted">Hỗ trợ VNPay</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h5>⚡ Giao Dịch Nhanh Chóng</h5>
                        <p class="text-muted">Nhận tài khoản qua email</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

---

### **PHASE 5: Testing** (30 minutes)

---

#### Task 5.1: Test Registration

**Steps:**
1. Start application: `cd game-account-shop && mvnw.cmd spring-boot:run`
2. Open browser: `http://localhost:8080/register`
3. Test scenarios:

| Test | Input | Expected Result |
|------|-------|-----------------|
| Valid registration | username: `testuser`, password: `password123`, email: `test@test.com` | Success message, redirect to login |
| Duplicate username | username: `admin`, password: `password123` | Error: "Tên đăng nhập đã tồn tại" |
| Short password | username: `test`, password: `123` | Error: "Mật khẩu phải có ít nhất 6 ký tự" |
| Empty field | Leave username empty | Validation error |

**Verify in database:**
```sql
mysql -u root -p gameaccountshop
SELECT * FROM users;
```

---

#### Task 5.2: Test Login

| Test | Input | Expected Result |
|------|-------|-----------------|
| Valid login | username: `admin`, password: `admin123` | Redirect to home, see "Xin chào, admin!" |
| Wrong password | username: `admin`, password: `wrong` | Error: "Tên đăng nhập hoặc mật khẩu không đúng" |
| Non-existent user | username: `nonexistent`, password: `test` | Error: "Tên đăng nhập hoặc mật khẩu không đúng" |

---

#### Task 5.3: Test Logout

1. While logged in, click "Đăng xuất" button
2. Verify redirect to login page
3. Verify "Đăng xuất thành công!" message
4. Verify you cannot access protected pages anymore

---

### **PHASE 6: Code Review** (30 minutes)

---

#### Task 6.1: Self-Review Checklist

Before submitting, verify:

- [ ] Code compiles without errors
- [ ] All acceptance criteria implemented
- [ ] Vietnamese error messages used
- [ ] Passwords are BCrypt hashed (not plaintext)
- [ ] Session timeout configured (30 minutes - from SecurityConfig)
- [ ] Registration assigns USER role (not ADMIN)
- [ ] All forms have proper validation
- [ ] Templates use Bootstrap for responsive design
- [ ] Navigation shows/hides based on login state
- [ ] Logout invalidates session

---

### **PHASE 7: Commit & Push** (15 minutes)

---

#### Task 7.1: Create Feature Branch and Commit

```bash
cd game-account-shop

# Create feature branch
git checkout main
git pull origin main
git checkout -b feature/story-1.2-user-registration-login

# Add and commit
git add .
git commit -m "Story 1.2: User Registration & Login

Changes:
- Created RegistrationRequest and LoginRequest DTOs
- Created UserService with BCrypt password hashing
- Created AuthController with registration/login/logout endpoints
- Updated SecurityConfig for form login
- Created Thymeleaf templates: register.html, login.html, index.html
- Added Bootstrap 5 for styling

Acceptance Criteria:
✅ Registration creates user with BCrypt password
✅ User assigned USER role
✅ Vietnamese error messages
✅ Session management with 30 min timeout
✅ Logout invalidates session

Tested:
- Registration with valid data: PASS
- Duplicate username validation: PASS
- Short password validation: PASS
- Login with valid credentials: PASS
- Login with invalid credentials: PASS
- Logout functionality: PASS"
```

#### Task 7.2: Push to GitHub

```bash
git push origin feature/story-1.2-user-registration-login
```

---

## 📚 References

| File | Location |
|------|----------|
| User Entity | `entity/User.java` (already exists) |
| Role Enum | `enums/Role.java` (already exists) |
| UserRepository | `repository/UserRepository.java` (already exists) |
| SecurityConfig | `config/SecurityConfig.java` (update needed) |
| project-context.md | `_bmad-output/planning-artifacts/project-context.md` |
| Database Schema | `docs/database-schema.md` |

---

## 🆘 Quick Help

### Common Issues

**Issue: "The bean 'UserService', defined in UserService, could not be autowired"**

**Solution:** Make sure you have `@Service` annotation on UserService class.

**Issue: "Field passwordEncoder requires a bean of type 'PasswordEncoder'"**

**Solution:** Already configured in SecurityConfig. Restart application.

**Issue: "Template might not exist or might not be accessible"**

**Solution:** Make sure templates are in `src/main/resources/templates/` folder.

**Issue: "Circular dependency"**

**Solution:** This shouldn't happen with this structure. If it does, ask Team Lead for help.

---

## ✅ Completion Checklist

Before marking story as done, verify:

- [ ] All acceptance criteria met
- [ ] Code compiles and runs
- [ ] All tests pass
- [ ] Vietnamese error messages used
- [ ] Security requirements met (BCrypt, sessions)
- [ ] Code follows project-context.md standards
- [ ] Committed to feature branch
- [ ] Pushed to GitHub
- [ ] Ready for code review

---

**Estimated Total Time:** 4-6 hours for a newbie developer

**Good luck! You've got this! 🚀**
