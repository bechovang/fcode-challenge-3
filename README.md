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

This section guides you through setting up MySQL 8.0 for the Game Account Shop project.

#### Step 2.1: Verify MySQL is Installed

**Check if MySQL is installed:**

```bash
# Windows
mysql --version

# Linux/Mac
mysql --version
```

**Expected output:**
```
mysql  Ver 8.0.xx for Win64 on x86_64
```

**If MySQL is NOT installed:**

**Windows:**
1. Go to https://dev.mysql.com/downloads/mysql/
2. Download "MySQL Community Server" 8.0
3. Run the installer
4. Use default settings (or set root password to something you'll remember)
5. Finish installation

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Mac (using Homebrew):**
```bash
brew install mysql
brew services start mysql
```

---

#### Step 2.2: Start MySQL Server

**Windows:**
- Open "Services" (press `Win + R`, type `services.msc`)
- Find "MySQL80" or "MySQL"
- Right-click → "Start"

**Or using command line:**
```bash
net start MySQL80
```

**Linux:**
```bash
sudo systemctl start mysql
```

**Mac:**
```bash
brew services start mysql
```

---

#### Step 2.3: Create the Database

**Option A: Let the application create it automatically (Easiest)**

The application is configured to create the database automatically if it doesn't exist. You don't need to do anything!

**Option B: Create manually using MySQL Command Line**

1. **Open MySQL Command Line:**

**Windows:**
- Open Command Prompt
- Type: `mysql -u root -p`
- Enter your MySQL root password (press Enter if no password set)

**Linux/Mac:**
```bash
mysql -u root -p
```

2. **Create the database:**

```sql
CREATE DATABASE gameaccountshop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

3. **Verify database was created:**

```sql
SHOW DATABASES;
```

You should see `gameaccountshop` in the list.

4. **Exit MySQL:**

```sql
EXIT;
```

---

#### Step 2.4: Verify Database Connection (Optional)

**Test that you can connect to the database:**

```bash
mysql -u root -p gameaccountshop
```

If successful, you'll see:
```
Server version: 8.0.xx MySQL Community Server
...
mysql>
```

Type `EXIT;` to leave.

---

#### Step 2.5: Configure Database Credentials

**The application uses these default credentials (already configured):**

```yaml
# In: game-account-shop/src/main/resources/application.yml
spring:
  datasource:
    username: root
    password: password
```

**If your MySQL root password is different:**

1. Open `game-account-shop/src/main/resources/application.yml` in a text editor
2. Find the `datasource` section
3. Change `password: password` to `password: YOUR_MYSQL_PASSWORD`

**Example:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: root
    password: MySecretPassword123  # Change this!
```

---

#### Step 2.6: Common MySQL Issues for Newbies

**Issue: "Access denied for user 'root'@'localhost'"**

**Solution:** Reset MySQL root password

**Windows:**
1. Stop MySQL service
2. Create a text file with: `ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewPassword123';`
3. Save as `reset.sql`
4. Run: `mysqld --init-file=C:\\path\\to\\reset.sql`
5. Start MySQL service

**Linux:**
```bash
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewPassword123';
FLUSH PRIVILEGES;
EXIT;
```

**Issue: "Can't connect to MySQL server on 'localhost'"

**Solution:** MySQL server is not running

**Windows:**
```bash
net start MySQL80
```

**Linux:**
```bash
sudo systemctl start mysql
```

**Issue: "Unknown database 'gameaccountshop'"

**Solution:** Database doesn't exist yet. The app will create it automatically, or you can create manually (see Step 2.3).

---

#### Step 2.7: Flyway Database Migrations (Automatic)

This project uses **Flyway** to manage database schema automatically.

**What happens on first run:**

1. Application starts
2. Flyway checks if migrations table exists
3. If not, it creates `flyway_schema_history` table
4. Runs migration script: `V1__Create_Database_Tables.sql`
5. Creates 4 tables: `users`, `game_accounts`, `transactions`, `reviews`

**You don't need to manually create tables!** Flyway handles everything.

**View migration script:**
```bash
# Location: game-account-shop/src/main/resources/db/migration/
cat V1__Create_Database_Tables.sql
```

---

#### Step 2.8: Verify Database is Ready

**After first application run, verify tables were created:**

```sql
mysql -u root -p gameaccountshop

SHOW TABLES;
```

**Expected output:**
```
+---------------------------+
| Tables_in_gameaccountshop |
+---------------------------+
| flyway_schema_history      |
| game_accounts             |
| reviews                    |
| transactions               |
| users                      |
+---------------------------+
```

**Check default admin user was created:**

```sql
SELECT id, username, role FROM users;
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

**Summary:**
1. ✅ Install MySQL 8.0 (if not installed)
2. ✅ Start MySQL server
3. ✅ Database will be created automatically (optional manual creation)
4. ✅ Configure password in `application.yml` if different from `password`
5. ✅ Flyway will create tables automatically on first run
6. ✅ Default admin account created automatically

### 3. Configure Database Connection

Edit `game-account-shop/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: root
    password: your_password_here  # Change this!
```

**Current default credentials in application.yml:**
- Username: `root`
- Password: `password`

### 4. Build and Run

**Navigate to project directory first:**

```bash
cd game-account-shop
```

**Then run using Maven wrapper:**

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Or using installed Maven (if you have Maven installed globally):**

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

Edit `game-account-shop/src/main/resources/application.yml`:

```yaml
server:
  port: 8081  # Change to available port
```

### Database Connection Failed

1. Verify MySQL is running: `mysql --version`
2. Check credentials in `game-account-shop/src/main/resources/application.yml`
3. Ensure database exists: `CREATE DATABASE gameaccountshop;`

### Flyway Migration Failed

Drop and recreate database:

```sql
DROP DATABASE gameaccountshop;
CREATE DATABASE gameaccountshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Project Won't Compile

```bash
cd game-account-shop
./mvnw clean compile
```

### Maven Wrapper Not Found

Make sure you're in the `game-account-shop` directory:

```bash
# Check current directory
pwd

# Should show: .../fcode-challenge-3
cd game-account-shop

# Now run
mvnw.cmd spring-boot:run
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
