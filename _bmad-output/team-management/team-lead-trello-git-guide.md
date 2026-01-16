# Team Lead Guide: Trello & Git Workflow

**Project:** Game Account Shop MVP
**Team:** 1 Team Lead + 3 Developers
**Tools:** Trello (Project Management), Git (Version Control)

---

## Part 1: Trello Setup & Management

### 1.1 Create Your Trello Board

**Step 1: Sign Up / Log In**
- Go to https://trello.com
- Create a free account (if needed)
- Log in

**Step 2: Create New Board**
1. Click "+ Create new board"
2. Board name: "Game Account Shop - MVP"
3. Choose a background color
4. Click "Create"

**Step 3: Create Lists**

Create these lists in order:
```
┌─────────────┬──────────┬──────────┬──────────┬───────────┬───────┬─────┐
│  Backlog    │ Sprint 1 │ Sprint 2 │ Sprint 3 │ In Progress│ Review│ Done│
└─────────────┴──────────┴──────────┴──────────┴───────────┴───────┴─────┘
```

**How to create a list:**
1. Click "Add a list"
2. Type the list name
3. Click "Save"
4. Repeat for all 7 lists

**What Each List Means:**
| List | Purpose | Cards |
|------|---------|-------|
| **Backlog** | All stories not yet assigned | All 21 stories initially |
| **Sprint 1** | Stories for current sprint | Stories from Epic 1 |
| **Sprint 2** | Stories for next sprint | Stories from Epic 2 |
| **Sprint 3** | Stories for third sprint | Stories from Epic 3 |
| **Sprint 4** | Stories for final sprint | Stories from Epic 4 |
| **In Progress** | Currently being worked on | Moved here when dev starts |
| **Review** | Completed, waiting for review | Moved here when dev says done |
| **Done** | Approved and complete | Moved here after your review |

---

### 1.2 Import Cards to Trello

**Option A: Manual Import (Recommended for First Time)**

Copy cards from `trello-cards.md` and create them in Trello:

**For each card:**
1. Click "+ Add a card" in the Backlog list
2. Paste the card title (e.g., "1.1: Initialize Spring Boot Project")
3. Click "Add card"
4. Click the card to open it
5. Paste the full description in the card description
6. Add labels (click "Labels" button)
7. Add due date if needed
8. Click "Save" and close

**Option B: Use Trello's Copy-Paste Feature**

1. Open `trello-cards.md`
2. Select all cards for a list
3. Copy the text
4. In Trello, position cursor where you want cards
5. Paste (Trello will create multiple cards)

**Option C: Trello Import (Requires Power-Up)**

1. Go to Board Menu → Add Power-Up
2. Search "Import"
3. Use CSV import if available

---

### 1.3 Set Up Labels

Create these labels for your board:

**Click "Labels" → "Create a new label":**

| Color | Name | Purpose |
|-------|------|---------|
| 🟢 Green | Frontend | UI/Thymeleaf work |
| 🔵 Blue | Backend | Service/Repository work |
| 🟣 Purple | Full-Stack | Controller + Frontend integration |
| 🔴 Red | Critical | Must have, blocking |
| 🟡 Yellow | Medium | Should have |
| ⚪ White | Low | Nice to have |

**How to add labels to cards:**
1. Open a card
2. Click "Labels" button
3. Select the appropriate label(s)
4. A card can have multiple labels (e.g., Blue + Red = Backend + Critical)

---

### 1.4 Assign Members to Cards

**Step 1: Invite Team Members to Board**
1. Click "Invite" button (top right)
2. Enter email addresses of your 3 developers
3. They will receive invitation to join

**Step 2: Assign Cards**
1. Open a card
2. Click "Members" button
3. Select the developer assigned to this card
4. The member's avatar will appear on the card

**Assignment Quick Reference:**
| Developer | Focus | Assign Cards With |
|-----------|-------|-------------------|
| Dev 1 | Backend | 🔵 Blue labels |
| Dev 2 | Frontend | 🟢 Green labels |
| Dev 3 | Full-Stack | 🟣 Purple labels |

---

### 1.5 Daily Trello Management

**Morning Standup Workflow:**

**Before standup:**
1. Open Trello board
2. Review all cards in "In Progress"
3. Check due dates (any overdue?)

**During standup:**
1. Ask each developer about their assigned cards
2. Update card statuses based on progress:
   - Card started → Move to "In Progress"
   - Card finished → Move to "Review"
   - Card blocked → Add red label + add comment

**After standup:**
1. Count cards in each list
2. Update your tracking sheet
3. Identify any blockers

---

### 1.6 Sprint Planning with Trello

**Weekly Sprint Planning Meeting:**

**Step 1: Review Completed Sprint**
1. Look at "Done" list
2. Count completed cards
3. Celebrate wins! 🎉

**Step 2: Select Stories for Next Sprint**
1. Move stories from "Sprint X" to "Sprint Y" list
2. Balance workload across developers
3. Consider dependencies (Story 1.2 must finish before 2.1)

**Step 3: Assign and Start**
1. Assign each story to a developer
2. Move first batch to "In Progress"
3. Developers start working

**Example Sprint 1 Setup:**
```
Move these cards from Backlog → Sprint 1:
- 1.1 Initialize Project
- 1.2 User Registration
- 1.3 User Login
- 1.4 User Logout
- 1.5 Default Admin

Assign all to Dev 1
```

---

### 1.7 Card Workflow (Team Lead Actions)

**When Developer Says "I'm starting this card":**
1. Move card from "Sprint X" → "In Progress"
2. Add start date in card description
3. No action needed if already assigned

**When Developer Says "This card is done":**
1. Move card from "In Progress" → "Review"
2. Add comment: "Ready for review - [Date]"
3. Schedule code review

**During Code Review:**
1. Open the card in "Review"
2. Test the feature
3. Check acceptance criteria

**If Approved:**
1. Move card from "Review" → "Done"
2. Add comment: "Approved ✅ - [Your Name]"
3. Congratulate the developer!

**If Needs Changes:**
1. Move card from "Review" → "In Progress"
2. Add comment with specific feedback
3. Don't assign blame - be constructive

---

### 1.8 Track Progress Daily

**Daily Progress Check:**

Create a simple daily report:

```
=== Daily Standup - [Date] ===

In Progress (3 cards):
- 1.2: User Registration (Dev 1) - 80% done
- 2.1: Create Listing (Dev 3) - Just started
- 2.3: Browse Listings (Dev 2) - Blocked on DB schema

Review (2 cards):
- 1.1: Initialize Project - Ready to test
- 1.3: User Login - Has bugs

Done (8 cards):
- All Epic 1 stories except those above

Blockers:
- Dev 2 waiting for Dev 1 to finish User entity
```

---

## Part 2: Git Repository Setup

### 2.1 Create GitHub Repository

**Step 1: Create GitHub Account**
1. Go to https://github.com
2. Sign up (free account)
3. Verify email

**Step 2: Create New Repository**
1. Click "+" → "New repository"
2. Repository name: `game-account-shop`
3. Description: "Game Account Shop MVP - Java Spring Boot + MySQL"
4. Choose **Private** (recommended)
5. DO NOT initialize with README (we'll add our code)
6. Click "Create repository"

**Step 3: Note Your Repository URL**
```
https://github.com/YOUR_USERNAME/game-account-shop.git
```

---

### 2.2 Team Git Setup

**Share These Instructions with Your Team:**

**For Each Developer:**

**Step 1: Install Git**
- Windows: Download from https://git-scm.com/
- Mac: `brew install git`
- Linux: `sudo apt install git`

**Step 2: Configure Git**
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

**Step 3: Generate SSH Key (One Time)**
```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
# Press Enter for all defaults

# Copy the SSH key
cat ~/.ssh/id_ed25519.pub
# Copy the output
```

**Step 4: Add SSH Key to GitHub**
1. Go to GitHub → Settings → SSH and GPG keys
2. Click "New SSH key"
3. Paste your key
4. Click "Add SSH key"

---

### 2.3 Clone Repository for Team

**Team Lead Only - Initial Setup:**

```bash
# Navigate to project directory
cd game-account-shop

# Initialize Git
git init

# Add all files
git add .

# First commit
git commit -m "Initial commit - Project initialized with Spring Initializr"

# Add remote (use YOUR URL)
git remote add origin git@github.com:YOUR_USERNAME/game-account-shop.git

# Push to main branch
git push -u origin main
```

**For Each Developer:**

```bash
# Clone the repository
git clone git@github.com:YOUR_USERNAME/game-account-shop.git

# Navigate into project
cd game-account-shop

# Verify
ls -la
# Should see all project files
```

---

### 2.4 Git Workflow for Team

**Branch Strategy:**

```
main (production)
  ↑
  │ (merge)
  │
feature/story-1.2-user-registration (Dev 1)
feature/story-2.1-create-listing (Dev 3)
feature/story-2.3-browse-listings (Dev 2)
```

**Rules:**
1. **main** branch is always stable
2. Developers work on **feature branches**
3. Each story = one feature branch
4. Branch naming: `feature/story-X.Y-description`

---

### 2.5 Developer Git Workflow

**Share This with Developers:**

**Step 1: Update Your Local Repository**
```bash
# Always start by pulling latest changes
git checkout main
git pull origin main
```

**Step 2: Create Feature Branch**
```bash
# Create branch for your story
git checkout -b feature/story-1.2-user-registration
```

**Step 3: Do Your Work**
- Write code
- Test locally
- Commit frequently

**Step 4: Commit Your Changes**
```bash
# Add changed files
git add .

# Commit with clear message
git commit -m "Story 1.2: Implement user registration

- Created User entity
- Created UserRepository
- Created UserService with BCrypt
- Created registration form
- Tested: registration works"
```

**Step 5: Push Your Branch**
```bash
git push origin feature/story-1.2-user-registration
```

**Step 6: Create Pull Request (on GitHub)**
1. Go to GitHub repository
2. Click "Pull requests"
3. Click "New pull request"
4. Select your branch
5. Add description: what you did, how to test
6. Click "Create pull request"

---

### 2.6 Code Review Process with Git

**As Team Lead, Review Pull Requests:**

**Step 1: Review the PR**
1. Go to GitHub "Pull requests" tab
2. Open the pull request
3. Review "Files changed" tab
4. Check code quality and patterns

**Step 2: Test the Feature**
```bash
# Checkout their branch locally
git fetch origin
git checkout feature/story-1.2-user-registration

# Run the app
./mvnw spring-boot:run

# Test the feature in browser
```

**Step 3: Approve or Request Changes**

**If Approved:**
1. Click "Merge pull request"
2. Click "Confirm merge"
3. Delete the branch

**If Changes Needed:**
1. Click "Review changes"
2. Add comments on specific lines
3. Click "Request changes"
4. Developer will fix and push again

---

### 2.7 Handling Merge Conflicts

**When Developers Have Conflicts:**

**Team Lead: Help resolve conflicts**

```bash
# 1. Update main first
git checkout main
git pull origin main

# 2. Merge feature branch
git merge feature/story-1.2-user-registration

# 3. If conflicts occur:
# Open conflicted files
# Look for <<<<<<< markers

# 4. Resolve conflicts manually
# Choose correct code
# Remove conflict markers

# 5. Mark as resolved
git add <conflicted-files>

# 6. Complete merge
git commit -m "Merge story-1.2 - resolved conflicts"

# 7. Push
git push origin main
```

---

## Part 3: Daily Workflow (Trello + Git Combined)

### Morning Standup Routine (15 minutes)

**Step 1: Open Trello**
- Check "In Progress" cards
- Review "Review" cards
- Note due dates

**Step 2: Ask Each Developer:**

For each developer:
1. "What Trello cards did you work on yesterday?"
2. "What's the Git commit message?"
3. "What are you working on today?"
4. "Any blockers?"

**Step 3: Update Trello**
- Move completed cards to "Review"
- Note blockers on cards (add red label)
- Update your tracking sheet

**Step 4: Check GitHub**
- Review open pull requests
- Review recent commits
- Check for any merge conflicts

---

### Weekly Sprint Review (1 hour)

**Step 1: Review Completed Work**
1. Open Trello "Done" list
2. Count completed cards
3. Review GitHub merged PRs

**Step 2: Demo Features**
1. Run the application
2. Demo completed stories
3. Get feedback from team

**Step 3: Plan Next Sprint**
1. Move cards from "Sprint X" to "Sprint Y"
2. Assign to developers
3. Create feature branches
4. Set due dates

---

## Part 4: Troubleshooting

### Trello Issues

**Issue: "I can't move cards"**
- Solution: Check if you're logged in
- Solution: Refresh the page

**Issue: "Cards are in wrong order"**
- Solution: Drag and drop to reorder
- Solution: Trello saves order automatically

**Issue: "Team member can't see board"**
- Solution: Re-send invitation
- Solution: Check if they accepted email

---

### Git Issues

**Issue: "Push rejected"**
```bash
# Pull first, then push
git pull origin main
git push origin main
```

**Issue: "Detached HEAD"**
```bash
git checkout main
git pull origin main
```

**Issue: "Merge conflict"**
- See Section 2.7 above

**Issue: "Can't push to main"**
- Solution: Check if branch is protected
- Solution: Use Pull Requests instead

---

## Part 5: Quick Reference Commands

### Trello Shortcuts

| Shortcut | Action |
|----------|--------|
| `N` | Create new card |
| `E` | Edit card |
| `C` | Archive card |
| `L` | Open labels menu |
| `Space` + `Enter` | Save and close |

### Git Commands

| Command | Purpose |
|---------|---------|
| `git status` | See changed files |
| `git pull` | Get latest changes |
| `git add .` | Stage all changes |
| `git commit -m "msg"` | Commit changes |
| `git push` | Send to GitHub |
| `git checkout -b branch` | Create branch |
| `git merge branch` | Merge branch |

---

## Part 6: Team Lead Checklist

### Before Project Starts
- [ ] Create Trello board
- [ ] Import all 21 cards
- [ ] Set up labels
- [ ] Invite team members
- [ ] Create GitHub repository
- [ ] Share repository URL with team

### Daily (Morning)
- [ ] Check Trello board
- [ ] Run standup meeting
- [ ] Update card statuses
- [ ] Check GitHub pull requests
- [ ] Handle blockers

### Weekly
- [ ] Sprint planning meeting
- [ ] Review completed stories
- [ ] Demo working features
- [ ] Plan next sprint
- [ ] Update tracking sheet

### Per Story
- [ ] Assign card to developer
- [ ] Create feature branch on GitHub
- [ ] Monitor progress
- [ ] Review pull request
- [ ] Test feature
- [ ] Merge to main
- [ ] Move card to Done

---

## Part 7: Communication Templates

### Trello Card Comment Template

```
@Developer moving this card to Review.

Please verify:
- [ ] All acceptance criteria met
- [ ] Manual testing completed
- [ ] Ready for review

Ready when you are!
```

### Pull Request Comment Template

```
Great work on Story X.Y! 🎉

I've reviewed the code and tested the feature. Everything looks good!

Approved. Merging to main.

Changes:
- Added User entity
- Implemented BCrypt password hashing
- Created registration form

Testing:
✅ Registration works
✅ Validation works
✅ Redirects to login

Merging now...
```

### Blocker Notification Template

```
🚨 BLOCKER ALERT

Card: Story X.Y - [Title]
Developer: @Developer
Issue: [Describe blocker]

Action needed:
[ ] What needs to happen

 ETA: [When will it be resolved]

cc: @Team
```

---

**You're all set! Your Trello board and Git repository are ready for your team to start building.**

**Good luck! 🚀**
