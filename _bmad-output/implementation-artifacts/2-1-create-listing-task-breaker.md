# Story 2.1: Create Listing (Simplified) - Task Breaker Guide

**For:** Newbie Developers
**Story ID:** 2-1
**Epic:** Listings & Ratings
**Estimated Time:** 3-4 hours

**🔄 Can be developed in PARALLEL with Stories 1.2 and 1.3**

---

## 📋 Story Overview

**User Story:**
As a logged-in user (seller), I want to create a new game account listing, so that buyers can see my account for sale.

**What You'll Build:**
1. GameAccount entity
2. Create Listing form page
3. ListingController for handling submissions
4. Form validation
5. Success/error messages in Vietnamese

---

## 🔄 Parallel Development Note

**This story can be worked on SIMULTANEOUSLY with Stories 1.2 and 1.3!**

| Story | Works On | Conflicts? |
|-------|----------|------------|
| 1.2 Registration/Login | User entity, AuthController, auth pages | ❌ No conflict |
| 1.3 Default Admin | DataInitializer, admin creation | ❌ No conflict |
| 2.1 Create Listing | GameAccount entity, ListingController, listing pages | ❌ No conflict |

**Why no conflicts?**
- Different entities (`User` vs `GameAccount`)
- Different controllers (`AuthController` vs `ListingController`)
- Different URL patterns (`/login`, `/register` vs `/listing/create`)
- Different HTML templates

**Team coordination:**
- Developer A: Works on 1.2
- Developer B: Works on 1.3
- Developer C: Works on 2.1 (this file)
- All can commit to different branches without merge conflicts

---

## 🎯 Acceptance Criteria Checklist

Use this to verify you've completed everything:

- [ ] Form shows: Game Name, Rank/Level, Price, Description
- [ ] All fields except description are required
- [ ] Price must be greater than 0
- [ ] Submit creates GameAccount with status "PENDING"
- [ ] seller_id is set to current logged-in user
- [ ] created_at timestamp is set automatically
- [ ] Success message: "Đăng bán thành công! Chờ admin duyệt."
- [ ] Error: "Vui lòng điền đầy đủ thông tin" (empty fields)
- [ ] Error: "Giá bán phải lớn hơn 0" (invalid price)
- [ ] User redirected to home page after success

---

## 📦 Task Breakdown

### **PHASE 1: Create GameAccount Entity** (45 minutes)

---

#### Task 1.1: Create GameAccount Entity

**What:** Database entity for game account listings

**File:** `src/main/java/com/gameaccountshop/entity/GameAccount.java`

**Key Concepts:**
- `@Entity` - Marks this class as a database table
- `@ManyToOne` - Links to the seller (User)
- `@Enumerated` - Stores ENUM as string (not ordinal)

```java
package com.gameaccountshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_accounts")
public class GameAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(name = "account_rank", nullable = false, length = 50)
    private String accountRank;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // Constructor
    public GameAccount() {
        this.createdAt = LocalDateTime.now();
        this.status = ListingStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
}
```

**Note:** We use `account_rank` instead of `rank` to avoid MySQL reserved keyword issues!

---

#### Task 1.2: Create ListingStatus Enum

**What:** Enumeration for listing status values

**File:** `src/main/java/com/gameaccountshop/enums/ListingStatus.java`

```java
package com.gameaccountshop.enums;

public enum ListingStatus {
    PENDING,    // Chờ admin duyệt
    APPROVED,   // Đã duyệt, hiển thị trên trang chủ
    REJECTED,   // Admin từ chối
    SOLD        // Đã bán
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 2: Create Repository** (15 minutes)

---

#### Task 2.1: Create GameAccountRepository

**What:** Data access layer for GameAccount

**File:** `src/main/java/com/gameaccountshop/repository/GameAccountRepository.java`

```java
package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // Find all APPROVED listings (for homepage)
    List<GameAccount> findByStatus(ListingStatus status);

    // Find listings by seller
    List<GameAccount> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    // Search by game name
    List<GameAccount> findByGameNameContainingIgnoreCaseAndStatus(String gameName, ListingStatus status);

    // Filter by rank
    List<GameAccount> findByAccountRankAndStatus(String accountRank, ListingStatus status);
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 3: Create DTO** (30 minutes)

---

#### Task 3.1: Create CreateListingRequest DTO

**What:** Data Transfer Object for listing creation form

**File:** `src/main/java/com/gameaccountshop/dto/CreateListingRequest.java`

```java
package com.gameaccountshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

public class CreateListingRequest {

    @NotBlank(message = "Tên game không được để trống")
    @Size(max = 100, message = "Tên game không quá 100 ký tự")
    private String gameName;

    @NotBlank(message = "Rank không được để trống")
    @Size(max = 50, message = "Rank không quá 50 ký tự")
    private String accountRank;

    @NotBlank(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0")
    @NumberFormat(pattern = "#,##0.00")
    private String price;

    private String description;

    // Getters and Setters
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Helper method to convert String price to BigDecimal
    public BigDecimal getPriceAsBigDecimal() {
        if (price == null || price.isBlank()) {
            return null;
        }
        // Remove commas and spaces, then parse
        String cleanPrice = price.replaceAll("[,\\s]", "");
        return new BigDecimal(cleanPrice);
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 4: Create Service** (45 minutes)

---

#### Task 4.1: Create ListingService

**What:** Business logic for creating listings

**File:** `src/main/java/com/gameaccountshop/service/ListingService.java`

```java
package com.gameaccountshop.service;

import com.gameaccountshop.dto.CreateListingRequest;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.GameAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ListingService {

    private final GameAccountRepository listingRepository;

    public ListingService(GameAccountRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Create a new game account listing
     * @param request Listing data from form
     * @param seller The logged-in user creating the listing
     * @return Created GameAccount
     */
    @Transactional
    public GameAccount createListing(CreateListingRequest request, User seller) {
        // Validate price
        BigDecimal price = request.getPriceAsBigDecimal();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }

        // Create new listing
        GameAccount listing = new GameAccount();
        listing.setSeller(seller);
        listing.setGameName(request.getGameName());
        listing.setAccountRank(request.getAccountRank());
        listing.setPrice(price);
        listing.setDescription(request.getDescription());

        // Status is PENDING by default (set in constructor)
        // CreatedAt is set by default

        return listingRepository.save(listing);
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 5: Create Controller** (45 minutes)

---

#### Task 5.1: Create ListingController

**What:** Web controller for listing operations

**File:** `src/main/java/com/gameaccountshop/controller/ListingController.java`

**Key Concepts:**
- `@PreAuthorize("isAuthenticated()")` - Only logged-in users can access
- Get current user from `SecurityContextHolder`

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.dto.CreateListingRequest;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ListingController {

    @Autowired
    private ListingService listingService;

    // Show create listing form
    @GetMapping("/listing/create")
    @PreAuthorize("isAuthenticated()")
    public String showCreateForm(Model model) {
        model.addAttribute("createListingRequest", new CreateListingRequest());
        return "create-listing";
    }

    // Process listing creation
    @PostMapping("/listing/create")
    @PreAuthorize("isAuthenticated()")
    public String createListing(
            @Valid @ModelAttribute("createListingRequest") CreateListingRequest request,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "create-listing";
        }

        try {
            // Get current logged-in user
            User currentUser = (User) authentication.getPrincipal();

            // Create listing
            listingService.createListing(request, currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Đăng bán thành công! Chờ admin duyệt.");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listing/create";
        }
    }
}
```

**✅ Verify:** Compile with `mvn compile`

---

### **PHASE 6: Update SecurityConfig** (15 minutes)

---

#### Task 6.1: Enable @PreAuthorize Annotations

**File:** `src/main/java/com/gameaccountshop/config/SecurityConfig.java`

**Add `@EnableMethodSecurity` annotation:**

```java
package com.gameaccountshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ADD THIS LINE
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/register", "/login", "/home", "/", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home?logout")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

---

### **PHASE 7: Create HTML Templates** (1 hour)

---

#### Task 7.1: Create Listing Form Template

**File:** `src/main/resources/templates/create-listing.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng bán tài khoản | Game Account Shop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" th:href="@{/}">🎮 Game Account Shop</a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <span class="navbar-text me-3" th:if="${#authentication.principal != 'anonymousUser'}">
                            Xin chào, <span th:text="${#authentication.principal.username}">User</span>!
                        </span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" th:href="@{/}">← Quay lại trang chủ</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0">📝 Đăng Bán Tài Khoản Game</h4>
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

                        <!-- Create Listing Form -->
                        <form th:action="@{/listing/create}" th:object="${createListingRequest}" method="post">
                            <!-- Game Name -->
                            <div class="mb-3">
                                <label for="gameName" class="form-label">Tên Game <span class="text-danger">*</span></label>
                                <input type="text" class="form-control"
                                       id="gameName"
                                       name="gameName"
                                       th:field="*{gameName}"
                                       placeholder="Ví dụ: Liên Minh Huyền Thoại, Valorant, Genshin Impact..."
                                       required>
                                <div class="form-text">Nhập tên chính xác của game</div>
                                <div th:if="${#fields.hasErrors('gameName')}" class="text-danger"
                                     th:errors="*{gameName}"></div>
                            </div>

                            <!-- Rank/Level -->
                            <div class="mb-3">
                                <label for="accountRank" class="form-label">Rank / Cấp độ <span class="text-danger">*</span></label>
                                <input type="text" class="form-control"
                                       id="accountRank"
                                       name="accountRank"
                                       th:field="*{accountRank}"
                                       placeholder="Ví dụ: Gold, Diamond, Challenger, Level 100..."
                                       required>
                                <div class="form-text">Rank hoặc cấp độ của tài khoản</div>
                                <div th:if="${#fields.hasErrors('accountRank')}" class="text-danger"
                                     th:errors="*{accountRank}"></div>
                            </div>

                            <!-- Price -->
                            <div class="mb-3">
                                <label for="price" class="form-label">Giá bán (VNĐ) <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <input type="text" class="form-control"
                                           id="price"
                                           name="price"
                                           th:field="*{price}"
                                           placeholder="Ví dụ: 500000, 1.000.000, 2000000"
                                           required>
                                    <span class="input-group-text">VNĐ</span>
                                </div>
                                <div class="form-text">Giá bán phải lớn hơn 0</div>
                                <div th:if="${#fields.hasErrors('price')}" class="text-danger"
                                     th:errors="*{price}"></div>
                            </div>

                            <!-- Description -->
                            <div class="mb-3">
                                <label for="description" class="form-label">Mô tả chi tiết</label>
                                <textarea class="form-control"
                                          id="description"
                                          name="description"
                                          th:field="*{description}"
                                          rows="5"
                                          placeholder="Mô tả thêm về tài khoản: số tướng, số skin, rune, trang bị..."></textarea>
                                <div class="form-text">Càng chi tiết, người mua càng dễ tin tưởng (không bắt buộc)</div>
                            </div>

                            <!-- Submit Button -->
                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    📢 Đăng Bán Ngay
                                </button>
                            </div>

                            <!-- Info Box -->
                            <div class="alert alert-info mt-3 mb-0">
                                <small>
                                    <strong>ℹ️ Lưu ý:</strong> Sau khi đăng, tài khoản sẽ được admin duyệt trước khi hiển thị công khai.
                                </small>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Price formatting script -->
    <script>
        document.getElementById('price').addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value) {
                value = parseInt(value).toLocaleString('vi-VN');
            }
            e.target.value = value;
        });
    </script>
</body>
</html>
```

---

#### Task 7.2: Update Home Page with "Create Listing" Button

**File:** `src/main/resources/templates/index.html` (update navigation section)

**Add this in the navbar when user is logged in:**

```html
<!-- In the navbar <ul>, add after logout button or before -->
<li class="nav-item" th:if="${#authentication.principal != 'anonymousUser'}">
    <a class="btn btn-success btn-sm mt-1" th:href="@{/listing/create}">
        ➕ Đăng Bán
    </a>
</li>
```

---

### **PHASE 8: Testing** (45 minutes)

---

#### Task 8.1: Create Listing Test

**Prerequisites:**
- Application running
- Logged in as a user (admin or regular user)

**Steps:**

1. **Navigate to create page:**
   - URL: `http://localhost:8080/listing/create`

2. **Test 1: Valid listing**
   - Game Name: `Liên Minh Huyền Thoại`
   - Rank: `Diamond`
   - Price: `500000`
   - Description: `Account có 100 tướng, 200 skin`
   - Click "Đăng Bán Ngay"
   - **Expected:** Success message, redirect to home

3. **Test 2: Empty required fields**
   - Leave "Game Name" empty
   - Click submit
   - **Expected:** Validation error, form shows again

4. **Test 3: Invalid price**
   - Price: `0` or `-100`
   - Click submit
   - **Expected:** Error "Giá bán phải lớn hơn 0"

5. **Verify in database:**
```sql
mysql -u root -p gameaccountshop
SELECT * FROM game_accounts ORDER BY created_at DESC LIMIT 1;
```

**Expected output:**
```
+----+-----------+--------------------------+--------------+-----------+-------------+---------+------------------+---------------------+
| id | seller_id | game_name                | account_rank | price      | description | status  | rejection_reason | created_at          |
+----+-----------+--------------------------+--------------+-----------+-------------+---------+------------------+---------------------+
|  1 |         1 | Liên Minh Huyền Thoại    | Diamond      | 500000.00 | Account...  | PENDING | NULL             | 2025-01-17 10:30... |
+----+-----------+--------------------------+--------------+-----------+-------------+---------+------------------+---------------------+
```

6. **Verify status is PENDING:**
   - The listing should NOT appear on the homepage yet (only APPROVED listings show)

---

#### Task 8.2: Access Control Test

**Test unauthorized access:**

1. **Logout** (if logged in)

2. **Try to access create page directly:**
   - URL: `http://localhost:8080/listing/create`
   - **Expected:** Redirected to login page

3. **Login as regular user** and try again
   - **Expected:** Create page loads successfully

---

### **PHASE 9: Code Review** (15 minutes)

---

#### Task 9.1: Self-Review Checklist

Before submitting, verify:

- [ ] GameAccount entity has all required fields
- [ ] `account_rank` used (not `rank` reserved keyword)
- [ ] ListingStatus enum created
- [ ] GameAccountRepository has query methods
- [ ] CreateListingRequest DTO has validation annotations
- [ ] ListingService validates price > 0
- [ ] ListingController uses @PreAuthorize
- [ ] Form has proper Vietnamese error messages
- [ ] Success message: "Đăng bán thành công! Chờ admin duyệt."
- [ ] Listing defaults to PENDING status
- [ ] seller_id is set to current logged-in user
- [ ] created_at is set automatically

---

### **PHASE 10: Integration with Other Stories** (Optional Reading)

---

#### How This Story Connects

```
Story 1.2 (User Auth)
        ↓
    Creates User entities
        ↓
Story 2.1 (This Story)
        ↓
    Uses User for seller_id
        ↓
    Creates GameAccount (PENDING)
        ↓
Story 2.4 (Admin Approve/Reject) ← Next story
        ↓
    Changes status to APPROVED
        ↓
Story 2.2 (Browse Listings) ← After 2.4
        ↓
    Shows only APPROVED listings
```

**Can work on Story 2.2 NEXT** (in parallel with 2.4, 2.5)

---

## 📚 References

| File | Location | Purpose |
|------|----------|---------|
| GameAccount | `entity/GameAccount.java` | NEW - Listing entity |
| ListingStatus | `enums/ListingStatus.java` | NEW - Status enum |
| GameAccountRepository | `repository/GameAccountRepository.java` | NEW - Data access |
| CreateListingRequest | `dto/CreateListingRequest.java` | NEW - Form DTO |
| ListingService | `service/ListingService.java` | NEW - Business logic |
| ListingController | `controller/ListingController.java` | NEW - Web endpoints |
| create-listing.html | `templates/create-listing.html` | NEW - Form page |
| SecurityConfig | `config/SecurityConfig.java` | UPDATE - Add @EnableMethodSecurity |

---

## 🆘 Quick Help

### Common Issues

**Issue: "Column 'rank' not found"**

**Solution:** We renamed the column to `account_rank` to avoid MySQL reserved keyword. Check:
- Entity: `private String accountRank;`
- Database: `account_rank VARCHAR(50)`

---

**Issue: "Cannot resolve method 'getPrincipal()' to User type"**

**Solution:** The Principal needs to be cast:
```java
User currentUser = (User) authentication.getPrincipal();
```

Make sure your `User` entity implements `UserDetails` or use:
```java
String username = authentication.getName();
User currentUser = userRepository.findByUsername(username).orElseThrow();
```

---

**Issue: "Price format error"**

**Solution:** The price comes as String from form. Use the helper method:
```java
BigDecimal price = request.getPriceAsBigDecimal();
```

This handles Vietnamese number format (1.000.000).

---

**Issue: "Access denied for /listing/create"**

**Solution:** Check SecurityConfig:
1. Has `@EnableMethodSecurity(prePostEnabled = true)`
2. User is logged in
3. Controller has `@PreAuthorize("isAuthenticated()")`

---

**Issue: "Listing not saving to database"**

**Solution:** Check:
1. `@Transactional` annotation on service method
2. `listingRepository.save(listing)` is called
3. No exceptions thrown (check console)
4. Database connection is working

---

## ✅ Completion Checklist

Before marking story as done, verify:

- [ ] All acceptance criteria met
- [ ] GameAccount entity created
- [ ] ListingStatus enum created
- [ ] Form validation works
- [ ] Vietnamese messages used
- [ ] Price > 0 validation
- [ ] Status defaults to PENDING
- [ ] Only authenticated users can access
- [ ] Database verifies correct data
- [ ] Code compiles and runs
- [ ] All tests pass
- [ ] Committed to feature branch

---

## 🎯 Git Commit

```bash
cd game-account-shop

# Create feature branch
git checkout main
git pull origin main
git checkout -b feature/story-2.1-create-listing

# Add and commit
git add .
git commit -m "Story 2.1: Create Listing (Simplified)

Changes:
- Created GameAccount entity with account_rank (not rank)
- Created ListingStatus enum (PENDING, APPROVED, REJECTED, SOLD)
- Created GameAccountRepository with query methods
- Created CreateListingRequest DTO with validation
- Created ListingService with business logic
- Created ListingController with @PreAuthorize
- Added @EnableMethodSecurity to SecurityConfig
- Created create-listing.html form template
- Updated index.html with 'Đăng Bán' button

Acceptance Criteria:
✅ Form shows: Game Name, Rank, Price, Description
✅ Creates GameAccount with status PENDING
✅ seller_id set to logged-in user
✅ Price > 0 validation
✅ Vietnamese error messages
✅ Success message: 'Đăng bán thành công! Chờ admin duyệt.'
✅ Only authenticated users can access

Tested:
- Valid listing creation: PASS
- Empty field validation: PASS
- Invalid price validation: PASS
- Access control (login required): PASS
- Database verification: PASS

Can be developed in parallel with Stories 1.2 and 1.3"

git push origin feature/story-2.1-create-listing
```

---

**Estimated Total Time:** 3-4 hours for a newbie developer

**🔄 Parallel Development:** This story can be worked on simultaneously with Stories 1.2 and 1.3 by different developers.

**Good luck! You've got this! 🚀**
