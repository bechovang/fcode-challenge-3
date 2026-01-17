# Developer Guide: Trello & Git Workflow

**Project:** Game Account Shop MVP
**Your Role:** Developer (Backend, Frontend, or Full-Stack)
**Tools:** Trello (Task tracking), Git (Version control), BMAD (AI workflows)

**Last Updated:** January 2026
**MVP Scope:** 14 stories across 4 epics

---

## Part 1: Trello for Developers

### 1.1 Access the Trello Board

**Getting Started:**

1. **Check your email** for Trello invitation from Team Lead
2. **Click "Accept Invitation"** in the email
3. **Create a Trello account** (if you don't have one)
4. **Log in** to trello.com
5. **Open the board:** "Game Account Shop - MVP"

**What You'll See:**

```
┌─────────────┬──────────┬──────────┬──────────┬───────────┬───────┐
│  Backlog    │ Sprint 1 │ Sprint 2,... │ In Progress│ Review  │ Done  │
│  (14 cards) │ (3 cards)│ (5 cards)│ (work!)   │(check it)│(done!)│
└─────────────┴──────────┴──────────┴──────────┴───────────┴───────┘
```

---

### 1.2 Find Your Assigned Cards

**How to See Your Work:**

1. Look for **your avatar** on cards (your initials or photo)
2. Filter by your name:
   - Click "Search cards" (top right)
   - Type "assigned to me"
   - Or look for cards with your avatar

**You'll typically be assigned 3-5 cards per sprint.**

**Example:**
- Dev A (Frontend): Cards with 🟢 green label
- Dev B (Backend): Cards with 🔵 blue label
- Dev C (Full-Stack): Cards with 🟣 purple label

---

### 1.3 Reading a Trello Card

**Open a card by clicking on it. You'll see:**

```
┌─────────────────────────────────────┐
│ Story 1.2: User Registration        │
│                                     │
│ Assigned to: @You                    │
│ Labels: 🔵 Backend 🔴 Critical      │
│ Due: [Date]                          │
│                                     │
│ DESCRIPTION:                         │
│ As a guest user,                    │
│ I want to register...               │
│                                     │
│ ACCEPTANCE CRITERIA:                │
│ ✅ Create user with valid data      │
│ ✅ Hash password with BCrypt        │
│ ✅ Assign USER role                 │
│                                     │
│ BMAD STORY FILE:                    │
│ _bmad-output/implementation-artifacts/│
│   1-2-user-registration-login.md    │
│                                     │
│ CHECKLIST:                           │
│ ☐ Create User entity                │
│ ☐ Create UserRepository             │
│ ☐ Create UserService                │
│ ☐ Create register.html              │
│ ☐ Test registration                 │
│                                     │
│ ACTIVITY:                            │
│ Team Lead: "Assigned to @You"       │
│ You: "Starting work now"            │
└─────────────────────────────────────┘
```

**What to Check:**
1. **Title** - What you're building
2. **Labels** - Type of work (Frontend/Backend/Full-Stack)
3. **Description** - Full story details
4. **Checklist** - Steps to complete
5. **BMAD Story File** - Reference to detailed story documentation
6. **Due date** - When it's due
7. **Comments** - Team communication

---

### 1.4 Using BMAD Workflows

**Before starting work, check if story file exists:**

```bash
# Story files are located at:
_bmad-output/implementation-artifacts/{story-key}.md

# Example:
_bmad-output/implementation-artifacts/1-2-user-registration-login.md
```

**If story file doesn't exist:**

1. Ask Team Lead to run: `/bmad:bmm:workflows:create-story`
2. Wait for story file to be created
3. Then start your work

**When implementing a story:**

Option 1: **Run dev-story workflow** (AI-assisted)
```
/bmad:bmm:workflows:dev-story
```
This will:
- Load the story file
- Guide you through implementation
- Run tests
- Update completion status

Option 2: **Manual implementation**
1. Read the story file
2. Implement acceptance criteria
3. Update checklist in Trello
4. Run tests locally
5. Commit your code

---

### 1.5 Updating Trello Cards

**When You Start Working:**

1. Open the card
2. Add a comment: "Starting work on this story"
3. Team Lead will move it to "In Progress"

**When You Complete a Task:**

1. Open the card's checklist
2. Check off completed items: ☐ → ☑
3. Add progress comments

**Example Progress Comment:**
```
Completed:
✅ Created User entity
✅ Created UserRepository
✅ Created UserService with BCrypt

Working on:
- Registration form (in progress)

ETA: Tomorrow afternoon
```

**When You Think It's Done:**

1. Open the card
2. Check all checklist items
3. Add a comment: "✅ Ready for review! Tested all acceptance criteria."
4. Team Lead will move it to "Review"

---

### 1.6 Daily Trello Routine

**Every Morning (Before Standup):**

1. **Open Trello board**
2. **Check your assigned cards in "In Progress"**
3. **Review checklist items**
4. **Prepare your standup update**

**Standup Format:**

```
Hi! Yesterday I completed:
- Story 1.2: User Registration ✅
  Created User entity, UserRepository, UserService
  All tests passing

Today I'm working on:
- Story 1.3: Default Admin Account
  Will create DataInitializer component

Blockers:
- None! / Waiting for [something]
```

**After Standup:**

1. Update your card comments if needed
2. Continue working on assigned cards

---

### 1.7 Moving Cards Through Workflow

**Card Lifecycle:**

```
Sprint 1 → In Progress → Review → Done
    ↓           ↓            ↓         ↓
  Assigned   Working    Testing   Approved
```

**What You Do:**
1. **Sprint 1** → Just assigned, not started
2. **In Progress** → You're working on it
3. **Review** → You finished, Team Lead is testing
4. **Done** → Approved and merged!

**Important:**
- You can move cards within "Sprint X" to prioritize
- Don't move cards between lists without telling Team Lead
- Always add comments when you make progress

---

## Part 2: Git for Developers

### 2.1 Initial Setup (One Time)

**Step 1: Install Git**

**Windows:**
1. Go to https://git-scm.com/
2. Download Windows installer
3. Run installer (use default settings)
4. Restart your computer

**Mac:**
```bash
brew install git
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install git
```

**Step 2: Configure Git**

Open terminal/command prompt:

```bash
# Set your name
git config --global user.name "Your Full Name"

# Set your email
git config --global user.email "your.email@example.com"

# Verify
git config --list
```

**Step 3: Generate SSH Key**

```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "your.email@example.com"

# Press Enter for all defaults
# Don't set a password (or you'll have to type it every time)

# Copy the key
cat ~/.ssh/id_ed25519.pub

# On Windows (if cat doesn't work):
# In Git Bash: cat ~/.ssh/id_ed25519.pub
# Or open file: C:\Users\YourName\.ssh\id_ed25519.pub
```

**Step 4: Add SSH Key to GitHub**

1. Go to https://github.com
2. Log in (or create account)
3. Click your avatar → Settings
4. Click "SSH and GPG keys" (left sidebar)
5. Click "New SSH key"
6. Paste your key (copy entire output from cat command)
7. Click "Add SSH key"

---

### 2.2 Clone the Repository

**Get the Repository URL from Team Lead:**

```
git@github.com:TEAM_LEAD_USERNAME/game-account-shop.git
```

**Clone the Repository:**

```bash
# Navigate to where you want the project
cd ~/projects

# Clone the repository
git clone git@github.com:USERNAME/game-account-shop.git

# Navigate into project
cd game-account-shop

# Verify
ls -la
# Should see: pom.xml, src/, .git/, etc.
```

---

### 2.3 Daily Git Workflow

**Every Morning - Start of Day:**

```bash
# Navigate to project
cd ~/projects/game-account-shop

# Check current branch
git branch

# Switch to main (if not already)
git checkout main

# Get latest changes
git pull origin main

# Verify you're up to date
git status
# Should say: "Your branch is up to date"
```

---

### 2.4 Create Feature Branch

**Before starting a new story:**

```bash
# Make sure you're on main
git checkout main

# Update main
git pull origin main

# Create feature branch
git checkout -b feature/story-1.2-user-registration

# Verify new branch
git branch
# Should show: * feature/story-1.2-user-registration
```

**Branch Naming Convention:**

```
feature/story-X.Y-short-description

Examples:
feature/story-1.2-user-registration
feature/story-2.1-create-listing
feature/story-3.2-vnpay-qr-code
```

---

### 2.5 Doing Your Work

**While Coding:**

1. **Write code** in your IDE
2. **Save files** regularly
3. **Test locally** (run `./mvnw spring-boot:run`)
4. **Commit frequently** (every 1-2 hours)

**Don't commit everything at once! Break it into logical steps:**

```bash
# Step 1: Create entity
# (code code code)
git add src/main/java/com/gameaccountshop/entity/User.java
git commit -m "Story 1.2: Created User entity

- Added id, username, password, email fields
- Added Role enum
- Added getters/setters"

# Step 2: Create repository
# (code code code)
git add src/main/java/com/gameaccountshop/repository/UserRepository.java
git commit -m "Story 1.2: Created UserRepository

- Extended JpaRepository
- Added findByUsername method"

# Step 3: Create service
# (code code code)
git add src/main/java/com/gameaccountshop/service/UserService.java
git commit -m "Story 1.2: Created UserService with BCrypt

- Implemented register method
- BCrypt password hashing
- Validation for duplicate username"
```

---

### 2.6 Committing Your Changes

**Step 1: Check What Changed**

```bash
git status

# Output:
# On branch feature/story-1.2-user-registration
# Changes not staged for commit:
#   modified:   User.java
#   modified:   UserService.java
#   new file:   AuthController.java
```

**Step 2: Review Changes (optional but recommended)**

```bash
# See what changed in a file
git diff User.java

# See all changes
git diff
```

**Step 3: Stage Your Changes**

```bash
# Add all changes
git add .

# OR add specific files
git add User.java UserService.java AuthController.java
```

**Step 4: Commit with Clear Message**

```bash
git commit -m "Story 1.2: Implemented user registration endpoint

Changes:
- Created AuthController with POST /auth/register
- Added form validation
- Added error handling
- Redirects to login after success

Acceptance Criteria:
✅ User created with valid data
✅ Password hashed with BCrypt
✅ User assigned USER role
✅ Success message displayed
✅ Validation for duplicate username
✅ Validation for short password

Tested:
- Registration with valid data: PASS
- Duplicate username: PASS
- Short password: PASS

Ready for review"
```

**Good Commit Messages:**
- Start with story number
- Summarize what you did
- List key changes
- Note what's tested

**Bad Commit Messages:**
```
"fixed stuff"
"updates"
"work"
```

---

### 2.7 Push Your Work

**When Your Story is Complete:**

```bash
# Push your branch to GitHub
git push origin feature/story-1.2-user-registration

# First time you'll see:
# Branch 'feature/story-1.2-user-registration' set up to track remote branch.
```

**What Push Does:**
- Sends your commits to GitHub
- Team Lead can see your work
- Ready for pull request

---

### 2.8 Code Review with BMAD

**Option 1: AI Code Review (Recommended)**

Before creating PR, run AI code review:

```bash
# Run code review workflow
/bmad:bmm:workflows:code-review
```

This will:
- Review your code against acceptance criteria
- Find potential issues
- Suggest fixes
- Update story status

**Option 2: Manual Pull Request**

**Step 1: Go to GitHub**

1. Open browser
2. Go to: https://github.com/USERNAME/game-account-shop
3. You'll see a banner: "feature/story-1.2-user-registration had recent pushes"
4. Click "Compare & pull request"

**Step 2: Create Pull Request**

**Fill in the form:**

```
Title: Story 1.2: User Registration

Description:
Implement user registration with BCrypt password hashing.

## What I Did
- Created User entity with username, password, email, role
- Created UserRepository with findByUsername method
- Created UserService with BCrypt password encoding
- Created AuthController with POST /auth/register endpoint
- Created register.html template with form
- Added validation for required fields
- Added validation for duplicate username
- Added validation for password length

## Acceptance Criteria
✅ User created with valid username, password, email
✅ Password hashed using BCrypt (10 rounds)
✅ User assigned "USER" role
✅ Redirects to login page after success
✅ Success message: "Đăng ký thành công!"
✅ Error: "Tên đăng nhập đã tồn tại"
✅ Error: "Mật khẩu phải có ít nhất 6 ký tự"

## How to Test
1. Run application: ./mvnw spring-boot:run
2. Open: http://localhost:8080/auth/register
3. Register with valid data → should work
4. Try duplicate username → should show error
5. Try short password → should show error

## Checklist
- [x] Code follows project conventions
- [x] All acceptance criteria met
- [x] Tested manually
- [x] No console errors
- [x] Ready for review
```

**Step 3: Submit**

1. Click "Create pull request"
2. Team Lead will get notification
3. Wait for review

---

### 2.9 During Code Review

**Team Lead Will:**
1. Review your code on GitHub
2. Test the feature locally
3. Leave comments if changes needed

**If Changes Requested:**

1. **Check GitHub comments** - Team Lead will leave feedback
2. **Make the changes** in your local code
3. **Commit the changes:**

```bash
git add .
git commit -m "Address review feedback

- Fixed issue X
- Updated validation as requested"
```

4. **Push again:**

```bash
git push origin feature/story-1.2-user-registration
```

5. **Pull request updates automatically**

**If Approved:**

1. Team Lead will merge your PR
2. Your code is now in `main` branch
3. **IMPORTANT:** Update your local main:

```bash
# Switch to main
git checkout main

# Pull latest changes
git pull origin main

# Delete your feature branch (optional)
git branch -d feature/story-1.2-user-registration
```

---

### 2.10 Handling Merge Conflicts

**When Git Says "Automatic Merge Failed":**

**Don't panic! This is normal.**

**Step 1: Update Your Branch**

```bash
git checkout feature/story-1.2-user-registration
git pull origin main
```

**Step 2: See Conflicts**

```bash
git status
# Shows: both modified: UserService.java
```

**Step 3: Open Conflicted File**

You'll see conflict markers:

```java
<<<<<<< HEAD
    // Your changes
    public User register(...) {
        // Your code
    }
=======
    // Someone else's changes
    public User register(...) {
        // Their code
    }
>>>>>>> main
```

**Step 4: Resolve Conflicts**

Edit the file to merge both changes:

```java
// MERGED VERSION
public User register(...) {
    // Combine both changes
    // Keep what makes sense
    // Delete conflict markers
}
```

**Remove all conflict markers:**
- `<<<<<<< HEAD`
- `=======`
- `>>>>>>> main`

**Step 5: Mark as Resolved**

```bash
git add UserService.java
```

**Step 6: Complete Merge**

```bash
git commit -m "Merge main into feature/story-1.2 - resolved conflicts"
```

**Step 7: Push**

```bash
git push origin feature/story-1.2-user-registration
```

**Still Stuck? Ask Team Lead for help!**

---

### 2.11 Common Git Commands

**Daily Commands:**

```bash
# See current status
git status

# See what branch you're on
git branch

# Switch branches
git checkout main
git checkout feature/story-X.Y

# Update from remote
git pull origin main

# See recent commits
git log --oneline -10
```

**Fix Mistakes:**

```bash
# Unstage a file
git reset HEAD file.java

# Undo last commit (keep changes)
git reset --soft HEAD~1

# See commit history
git log

# See changes in last commit
git show HEAD
```

**Get Help:**

```bash
# Git help
git help commit

# Or use Google
# Search: "git how to undo commit"
```

---

## Part 3: Daily Workflow (Trello + Git Combined)

### Morning Routine (10 minutes)

**Every Day When You Start Work:**

```bash
# 1. Update Git
cd ~/projects/game-account-shop
git checkout main
git pull origin main

# 2. Check Trello
# - Open browser to Trello board
# - Check "In Progress" cards
# - See what's assigned to you
```

**Standup Update:**

```
Yesterday: Story 1.2 - User Registration
  Created User entity and UserRepository ✅
  Pushed to feature branch ✅

Today: Story 1.2 - User Registration
  Finishing UserService and AuthController
  Creating registration form

Blockers: None!
```

---

### During the Day

**While Working:**

1. **Code in your IDE**
2. **Commit every 1-2 hours**
3. **Update Trello card comments**
4. **Test your work**

**Example:**

```
10:00 AM - Starting work
→ Create feature branch
→ Start coding User entity

11:30 AM - First commit
→ git add User.java
→ git commit -m "Created User entity"
→ Update Trello card comment

1:00 PM - Lunch break 🍕

2:00 PM - Continue work
→ Code UserRepository

3:30 PM - Second commit
→ git add UserRepository.java
→ git commit -m "Created UserRepository"
→ Update Trello: Checked off 2 items

5:00 PM - End of day
→ Push to GitHub
→ Update Trello with tomorrow's plan
```

---

### When Story is Complete

**Final Steps:**

```bash
# 1. Run code review (optional)
/bmad:bmm:workflows:code-review

# 2. Final commit
git add .
git commit -m "Story 1.2: Complete - All acceptance criteria met"

# 3. Push to GitHub
git push origin feature/story-1.2-user-registration

# 4. Create Pull Request on GitHub
# 5. Update Trello card: "✅ Ready for review!"

# 6. Notify Team Lead
# (In standup or via chat)
```

---

## Part 4: Project Structure Reference

### Current MVP Scope (14 Stories)

| Epic | Stories | Status |
|------|---------|--------|
| Epic 1: Basic Authentication | 3 stories | In Progress (1/3 done) |
| Epic 2: Listings & Ratings | 5 stories | Backlog |
| Epic 3: Simple Buying | 3 stories | Backlog |
| Epic 4: Dashboard & Profiles | 3 stories | Backlog |

### Story Files Location

```
_bmad-output/implementation-artifacts/
├── 1-1-initialize-spring-boot-project.md ✅ DONE
├── 1-2-user-registration-login.md
├── 1-3-default-admin-account.md
├── 2-1-create-listing.md
├── 2-2-browse-listings-search-filter.md
├── 2-3-listing-details-rating.md
├── 2-4-admin-approve-reject-listings.md
├── 2-5-mark-listing-sold.md
├── 3-1-buyer-click-buy.md
├── 3-2-show-vnpay-qr-code.md
├── 3-3-admin-verify-send-gmail.md
├── 4-1-simple-admin-dashboard.md
├── 4-2-seller-profile-page.md
├── 4-3-my-purchases-page.md
└── sprint-status.yaml
```

### Code Structure

```
game-account-shop/
├── src/main/java/com/gameaccountshop/
│   ├── GameAccountShopApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── DataInitializer.java
│   ├── controller/
│   ├── service/
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   ├── enums/
│   │   └── Role.java
│   └── dto/
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/
│   │   └── V1__Create_Database_Tables.sql
│   ├── templates/
│   └── static/
└── pom.xml
```

---

## Part 5: Troubleshooting

### Git Issues

**Issue: "Permission denied (publickey)"**

```bash
# Your SSH key isn't set up correctly
# Generate SSH key again
ssh-keygen -t ed25519 -C "your.email@example.com"

# Add to GitHub
cat ~/.ssh/id_ed25519.pub
# Copy and paste to GitHub Settings → SSH keys
```

**Issue: "Failed to push some refs"**

```bash
# Someone else pushed before you
# Pull first, then push
git pull origin main
git push origin feature-branch
```

**Issue: "Nothing to commit"**

```bash
# You haven't made any changes
# Or changes are already committed
git status
```

**Issue: Merge conflict**

```bash
# See Section 2.10 above
# Or ask Team Lead for help
```

---

### Trello Issues

**Issue: "I can't find my cards"**

- Search for your name in Trello
- Check with Team Lead
- Look in "Sprint X" lists

**Issue: "Card is missing"**

- It might have been moved to "Review" or "Done"
- Check with Team Lead
- Search for card title

**Issue: "Can't add comment"**

- Make sure you're logged in
- Refresh the page
- Try again

---

### Build Issues

**Issue: "Compilation failed"**

```bash
# Check Java version
java -version
# Should be 17+

# Clean and rebuild
./mvnw clean compile

# If still failing, check for syntax errors
```

**Issue: "Database connection failed"**

```bash
# Check MySQL is running
mysql --version

# Verify database exists
mysql -u root -p
> SHOW DATABASES;

# Check application.yml credentials
```

---

## Part 6: Quick Reference

### Git Command Summary

| Command | What It Does |
|---------|--------------|
| `git status` | See what's changed |
| `git pull` | Get latest from GitHub |
| `git add .` | Stage all changes |
| `git commit -m "msg"` | Save changes |
| `git push` | Send to GitHub |
| `git checkout -b branch` | Create new branch |
| `git checkout main` | Switch to main |
| `git log` | See commit history |

### BMAD Workflows

| Command | What It Does |
|---------|--------------|
| `/bmad:bmm:workflows:create-story` | Create story file from epic |
| `/bmad:bmm:workflows:dev-story` | AI-assisted story implementation |
| `/bmad:bmm:workflows:code-review` | AI code review |
| `/bmad:bmm:workflows:sprint-status` | Check sprint progress |

### Trello Card Labels

| Color | Meaning |
|-------|---------|
| 🟢 Green | Frontend work |
| 🔵 Blue | Backend work |
| 🟣 Purple | Full-stack work |
| 🔴 Red | Critical |
| 🟡 Yellow | Medium |
| ⚪ White | Low priority |

---

## Part 7: Best Practices

### Do's ✅

- **Commit frequently** (every 1-2 hours)
- **Write clear commit messages**
- **Update Trello card comments**
- **Test before committing**
- **Pull before pushing**
- **Use BMAD workflows** for story implementation
- **Follow project-context.md** coding standards
- **Ask for help when stuck**

### Don'ts ❌

- **Don't commit broken code**
- **Don't push without testing**
- **Don't work on main branch**
- **Don't forget to pull latest changes**
- **Don't commit sensitive data (passwords, keys)**
- **Don't ignore merge conflicts**
- **Don't skip code review**

---

## Part 8: Getting Help

### When to Ask for Help

**Ask immediately when:**
- Stuck for more than 30 minutes
- Git error you don't understand
- Not sure about acceptance criteria
- Merge conflict you can't resolve
- Test is failing and you don't know why
- BMAD workflow not working as expected

### How to Ask

**Good way:**
```
I'm working on Story 1.2: User Registration

I've completed:
✅ Created User entity
✅ Created UserRepository

I'm stuck on:
- UserService with BCrypt

Error message:
[paste error]

What I tried:
[what you attempted]

Can someone help?
```

**Bad way:**
```
It doesn't work. Help!
```

---

## Part 9: End of Day Checklist

**Before you leave each day:**

- [ ] Commit all your changes
- [ ] Push to GitHub
- [ ] Update Trello card comments
- [ ] Note what you'll do tomorrow
- [ ] No uncommitted changes left

**Verify:**

```bash
git status
# Should say: "nothing to commit, working tree clean"
```

---

## Part 10: Story Completion Checklist

**Before marking a story as complete:**

- [ ] All acceptance criteria implemented
- [ ] Code tested locally
- [ ] No console errors or warnings
- [ ] Follows project-context.md standards
- [ ] Code committed to feature branch
- [ ] Pushed to GitHub
- [ ] Trello checklist completed
- [ ] Ready for code review

**After code review approved:**

- [ ] Pull request merged to main
- [ ] Local main branch updated
- [ ] Feature branch deleted (optional)
- [ ] Story marked as "done" in sprint-status.yaml
- [ ] Trello card moved to "Done"

---

**You're ready! Follow this guide and you'll be a Trello & Git pro in no time! 🚀**

**Remember: When in doubt, ask your Team Lead!**

---

**Documentation Links:**
- Database Schema: `docs/database-schema.md`
- Project Context: `_bmad-output/planning-artifacts/project-context.md`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`
- Epics: `_bmad-output/planning-artifacts/epics.md`
- Sprint Status: `_bmad-output/implementation-artifacts/sprint-status.yaml`
