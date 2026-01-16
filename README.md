# Game Account Shop - MVP

A beginner-friendly game account marketplace built with Spring Boot 3.5.0 and Java 17. Sellers can list game accounts, buyers can browse and purchase, and admins manage the platform.

## 🎯 Project Overview

**Game Account Shop** is a Vietnamese marketplace platform where:
- **Sellers** list game accounts (Liên Minh Huyền Thoại, Valorant, etc.)
- **Buyers** browse listings, search/filter, and purchase via VNPay QR code
- **Admins** approve listings, verify payments, and deliver credentials via email

**Target Audience:** Newbie developers learning Spring Boot

**Technology Stack:**
- **Backend:** Java 17, Spring Boot 3.5.0, Spring Data JPA, Spring Security, MySQL 8.0
- **Frontend:** Thymeleaf, Bootstrap 5.3, HTML5/CSS3/JavaScript
- **Database:** MySQL 8.0 with Flyway migrations
- **Build:** Maven 3.x

---

## 📋 Prerequisites

Before running this project, ensure you have:

| Tool | Version | Command to Check |
|------|---------|------------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| MySQL | 8.0+ | `mysql --version` |
| Git | Latest | `git --version` |

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd fcode-challenge-3
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE gameaccountshop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Or let the application create it automatically (configured in `application.yml`).

### 3. Configure Database Connection

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: root
    password: your_password_here  # Change this!
```

### 4. Build and Run

Using Maven wrapper:

```bash
cd game-account-shop

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Or using installed Maven:

```bash
mvn spring-boot:run
```

### 5. Verify Startup

Look for this log message:

```
=================================================================
DEFAULT ADMIN ACCOUNT CREATED
Username: admin
Password: admin123
=================================================================
```

### 6. Access the Application

Open your browser: **http://localhost:8080**

---

## 🔑 Default Admin Account

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | ADMIN |

**⚠️ IMPORTANT:** Change the default admin password after first login!

---

## 📁 Project Structure

```
game-account-shop/
├── src/
│   ├── main/
│   │   ├── java/com/gameaccountshop/
│   │   │   ├── GameAccountShopApplication.java    # Main entry point
│   │   │   ├── config/                            # Configuration classes
│   │   │   │   ├── SecurityConfig.java            # Security + BCrypt
│   │   │   │   └── DataInitializer.java           # Creates default admin
│   │   │   ├── controller/                        # Web controllers
│   │   │   ├── service/                           # Business logic
│   │   │   ├── repository/                        # Data access (JPA)
│   │   │   ├── entity/                            # Database entities
│   │   │   │   └── User.java
│   │   │   ├── enums/                             # Enumerations
│   │   │   │   └── Role.java                      # USER, ADMIN
│   │   │   └── dto/                               # Data Transfer Objects
│   │   └── resources/
│   │       ├── application.yml                    # Application config
│   │       ├── db/migration/                      # Flyway migrations
│   │       │   └── V1__Create_Database_Tables.sql
│   │       ├── templates/                         # Thymeleaf templates
│   │       └── static/                            # CSS, JS, images
│   └── test/                                      # Unit tests
├── pom.xml                                        # Maven dependencies
├── mvnw, mvnw.cmd                                 # Maven wrapper scripts
└── .mvn/                                          # Wrapper files
```

---

## 🗄️ Database Schema

### Tables

| Table | Description |
|-------|-------------|
| `users` | User accounts (buyers, sellers, admins) |
| `game_accounts` | Game account listings |
| `transactions` | Purchase transactions |
| `reviews` | Seller ratings and reviews |

### Status Flows

**Game Account Status:**
```
PENDING → APPROVED → SOLD
    ↓
  REJECTED
```

**Transaction Status:**
```
PENDING → VERIFIED
```

**User Roles:**
```
USER (can be seller or buyer)
ADMIN
```

---

## 📚 Documentation

| Document | Location | Description |
|----------|----------|-------------|
| **Database Schema** | `docs/database-schema.md` | Complete database design |
| **Project Context** | `_bmad-output/planning-artifacts/project-context.md` | Coding standards & rules |
| **Architecture** | `_bmad-output/planning-artifacts/architecture.md` | System architecture decisions |
| **Epics & Stories** | `_bmad-output/planning-artifacts/epics.md` | Feature breakdown (14 stories) |
| **Sprint Status** | `_bmad-output/implementation-artifacts/sprint-status.yaml` | Development progress |

---

## 🛠️ Development Guidelines

### Coding Standards

**Follow the `project-context.md` rules:**

1. **Naming Conventions:**
   - Classes: `PascalCase` → `UserService`
   - Methods: `camelCase` → `getUserById`
   - Database: `snake_case` → `created_at`, `seller_id`

2. **Architecture Pattern (Strict Layered):**
   ```
   Browser → Controller → Service → Repository → Database
   ```

3. **Security:**
   - BCryptPasswordEncoder with 10 rounds minimum
   - Never store plaintext passwords
   - Session timeout: 30 minutes

4. **Error Messages:**
   - All user-facing messages in **Vietnamese**
   - Example: `"Tên đăng nhập hoặc mật khẩu không đúng"`

### Creating a New Story

```bash
# Use the BMAD workflow
/bmad:bmm:workflows:create-story
```

### Running Development Workflow

```bash
# Execute a story
/bmad:bmm:workflows:dev-story

# Code review
/bmad:bmm:workflows:code-review
```

---

## 🧪 Testing

Run tests:

```bash
mvn test
```

Run with coverage:

```bash
mvn clean test jacoco:report
```

---

## 🐛 Troubleshooting

### Port 8080 Already in Use

Edit `application.yml`:

```yaml
server:
  port: 8081  # Change to available port
```

### Database Connection Failed

1. Verify MySQL is running: `mysql --version`
2. Check credentials in `application.yml`
3. Ensure database exists: `CREATE DATABASE gameaccountshop;`

### Flyway Migration Failed

Drop and recreate database:

```sql
DROP DATABASE gameaccountshop;
CREATE DATABASE gameaccountshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📝 Current Status

| Epic | Status | Stories Done |
|------|--------|--------------|
| Epic 1: Basic Authentication | In Progress | 1/3 (✅Initialize) |
| Epic 2: Listings & Ratings | Backlog | 0/5 |
| Epic 3: Simple Buying | Backlog | 0/3 |
| Epic 4: Dashboard & Profiles | Backlog | 0/3 |

---

## 🤝 Contributing

This is a learning project for newbie developers. To contribute:

1. Check `sprint-status.yaml` for available stories
2. Pick a story from backlog
3. Run `/bmad:bmm:workflows:dev-story` to implement
4. Run `/bmad:bmm:workflows:code-review` for review
5. Update sprint status when complete

---

## 📞 Support

For questions or issues:
- Review `docs/database-schema.md` for data model questions
- Check `project-context.md` for coding standards
- Refer to `epics.md` for feature specifications

---

## 📄 License

This project is for educational purposes.

---

**Built with ❤️ for newbie developers learning Spring Boot**
