# Team Lead Guide - Game Account Shop MVP

**Project:** Game Account Shop (fcode project)
**Team Size:** 4 people (1 Team Lead + 3 Developers)
**Duration:** MVP (estimated based on story complexity)

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Team Structure](#team-structure)
3. [Task Management](#task-management)
4. [Sprint Planning](#sprint-planning)
5. [Daily Operations](#daily-operations)
6. [Code Review Process](#code-review-process)
7. [Quality Assurance](#quality-assurance)
8. [Issue Resolution](#issue-resolution)
9. [Delivery Checklist](#delivery-checklist)

---

## Project Overview

### What We're Building

A **simplified MVP** for a game account marketplace where:
- Users can register and log in
- Sellers can post game account listings
- Admin approves listings
- Buyers can purchase accounts using VNPay QR codes
- Admin manually verifies payments
- Sellers deliver credentials to buyers

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Backend | Java | 17 |
| Framework | Spring Boot | 3.5.0 |
| Database | MySQL | 8.0 |
| Build Tool | Maven | - |
| Frontend | HTML/CSS/JS + Bootstrap 5 | 5.3+ |
| Templating | Thymeleaf | - |

### Scope: 21 User Stories Across 4 Epics

| Epic | Stories | Focus Area |
|------|---------|------------|
| Epic 1 | 5 stories | Project Setup & Authentication |
| Epic 2 | 7 stories | Account Listings Management |
| Epic 3 | 5 stories | Buying Process |
| Epic 4 | 4 stories | Admin Dashboard |

---

## Team Structure

### Roles & Responsibilities

**Team Lead (You):**
- Overall project coordination
- Task assignment and prioritization
- Code review approval
- Blocking issue resolution
- Client/stakeholder communication
- Quality assurance

**Developer 1 (Backend Focus):**
- Entity and Repository layer
- Service layer business logic
- Security configuration
- Database setup

**Developer 2 (Frontend Focus):**
- Thymeleaf templates
- Bootstrap UI implementation
- JavaScript interactions
- Responsive design

**Developer 3 (Full-Stack/Integration):**
- Controller layer
- API integration
- End-to-end workflows
- Admin dashboard

### Communication Channels

**Daily Standup (15 minutes):**
- Time: Morning (e.g., 9:30 AM)
- Format: Each person shares:
  - What I completed yesterday
  - What I'm working on today
  - Any blockers I need help with

**Weekly Sprint Review:**
- Review completed stories
- Demo working features
- Plan next sprint

**Communication Tools:**
- Use your preferred tool (Slack, Discord, WhatsApp, etc.)
- Create channels: #general, #backend, #frontend, #bugs

---

## Task Management

### How to Use This Guide

1. **Assign stories to developers** based on their skills
2. **Track progress** using the assignment table below
3. **Update status** as stories move through stages
4. **Handle blockers** immediately when identified

### Story Assignment Template

Copy this table to track assignments:

| Story | Assigned To | Status | Started | Completed | Notes |
|-------|-------------|--------|---------|-----------|-------|
| 1.1: Initialize Project | Dev 1 | 🔄 | MM/DD | MM/DD | Spring Initializr setup |
| 1.2: User Registration | Dev 1 | ⬜ | - | - | - |
| 1.3: User Login | Dev 1 | ⬜ | - | - | - |
| ... | ... | ... | ... | ... | ... |

**Status Legend:**
- ⬜ Not Started
- 🔄 In Progress
- ⏸️ Blocked
- ✅ Complete
- ❌ Failed/Needs Rework

### Recommended Assignment (Based on Skills)

**Epic 1: Project Setup & Authentication**
| Story | Assign To | Estimated Duration |
|-------|-----------|-------------------|
| 1.1: Initialize Project | Dev 1 | 0.5 day |
| 1.2: User Registration | Dev 1 | 1 day |
| 1.3: User Login | Dev 1 | 1 day |
| 1.4: User Logout | Dev 1 | 0.5 day |
| 1.5: Default Admin | Dev 1 | 0.5 day |

**Epic 2: Account Listings Management**
| Story | Assign To | Estimated Duration |
|-------|-----------|-------------------|
| 2.1: Create Listing | Dev 3 | 1 day |
| 2.2: My Listings | Dev 2 | 0.5 day |
| 2.3: Browse Listings | Dev 2 | 1 day |
| 2.4: Listing Details | Dev 2 | 0.5 day |
| 2.5: Admin Queue | Dev 3 | 0.5 day |
| 2.6: Approve Listing | Dev 3 | 0.5 day |
| 2.7: Reject Listing | Dev 3 | 0.5 day |

**Epic 3: Buying Process**
| Story | Assign To | Estimated Duration |
|-------|-----------|-------------------|
| 3.1: Initiate Purchase | Dev 3 | 1 day |
| 3.2: VNPay QR Code | Dev 1 | 1 day |
| 3.3: Verify Payment | Dev 3 | 0.5 day |
| 3.4: Deliver Credentials | Dev 2 | 0.5 day |
| 3.5: Receive Credentials | Dev 2 | 0.5 day |

**Epic 4: Admin Dashboard**
| Story | Assign To | Estimated Duration |
|-------|-----------|-------------------|
| 4.1: Dashboard Overview | Dev 3 | 1 day |
| 4.2: View Users | Dev 2 | 0.5 day |
| 4.3: Transactions | Dev 2 | 0.5 day |
| 4.4: Activity Log | Dev 1 | 0.5 day |

**Total Estimated Effort:** ~14 working days for 3 developers

---

## Sprint Planning

### How to Run Sprints

**Sprint Length:** 1 week (5 working days)
**Sprint Goal:** Complete specific stories from the backlog

### Sprint Planning Steps

**Step 1: Review Available Stories**
- Look at remaining stories from epics.md
- Check dependencies (Story 1.2 must complete before 2.1)

**Step 2: Select Stories for Sprint**
- Choose stories that can be completed in 1 week
- Balance workload across all 3 developers
- Consider story dependencies

**Step 3: Assign Stories**
- Assign each story to a developer
- Clear acceptance criteria review
- Answer questions before starting

**Step 4: Track Daily**
- Update status in daily standup
- Move stories across Kanban board (Todo → In Progress → Review → Done)

### Example Sprint 1 Plan

**Goal:** Complete Epic 1 (Authentication) and start Epic 2

| Story | Assigned | Priority |
|-------|----------|----------|
| 1.1: Initialize Project | Dev 1 | Must Have |
| 1.2: User Registration | Dev 1 | Must Have |
| 1.3: User Login | Dev 1 | Must Have |
| 1.4: User Logout | Dev 1 | Should Have |
| 1.5: Default Admin | Dev 1 | Should Have |

**Sprint 1 Success Criteria:**
- ✅ Project runs on localhost:8080
- ✅ Users can register
- ✅ Users can log in/logout
- ✅ Admin account created

### Example Sprint 2 Plan

**Goal:** Complete Epic 2 (Listings)

| Story | Assigned | Priority |
|-------|----------|----------|
| 2.1: Create Listing | Dev 3 | Must Have |
| 2.2: My Listings | Dev 2 | Must Have |
| 2.3: Browse Listings | Dev 2 | Must Have |
| 2.4: Listing Details | Dev 2 | Should Have |
| 2.5: Admin Queue | Dev 3 | Should Have |
| 2.6: Approve Listing | Dev 3 | Should Have |
| 2.7: Reject Listing | Dev 3 | Nice to Have |

---

## Daily Operations

### Daily Standup Template

Run this every morning. Each person answers 3 questions:

**[Developer Name]**

1. **Yesterday I completed:**
   - Story X.X: [specific work done]

2. **Today I'm working on:**
   - Story Y.Y: [what needs to be done]

3. **Blockers I need help with:**
   - [Any issues preventing progress]

**Team Lead Actions:**
- Note blockers and assign someone to help
- Adjust assignments if someone is blocked
- Update the assignment tracking table

### What to Do When Someone is Blocked

**Priority: Handle blockers immediately**

1. **Identify the blocker type:**
   - Technical issue → Assign Dev 1 or Dev 3 to help
   - Missing requirements → Team Lead clarifies
   - Dependency on another story → Reorder tasks

2. **Create a help session:**
   - Pair programming for 1 hour
   - Code review together
   - Whiteboard the solution

3. **Follow up:**
   - Check if blocker is resolved in next standup
   - If not, escalate or reassign

---

## Code Review Process

### When to Review Code

**Review Threshold:** Every story must be reviewed before marking "Complete"

**Review Process:**

1. **Developer submits:**
   - "Story X.X is ready for review"
   - Shares Git branch or shows working demo

2. **Team Lead reviews:**
   - Check acceptance criteria
   - Run the application
   - Test the feature
   - Check code quality

3. **Decision:**
   - ✅ Approve → Mark story complete, merge to main
   - ❌ Reject → List changes needed, send back for fixes

### Code Review Checklist

For each story, check:

**Functionality:**
- [ ] All acceptance criteria met
- [ ] Feature works as described
- [ ] No obvious bugs when testing

**Code Quality:**
- [ ] Follows naming conventions (see Developer Guide)
- [ ] No hardcoded values (use constants)
- [ ] Proper error handling
- [ ] Vietnamese error messages for user-facing text

**Integration:**
- [ ] No breaking changes to existing features
- [ ] Database migrations included if needed
- [ ] Tests pass (if tests are written)

**Documentation:**
- [ ] Code is readable and self-documenting
- [ ] Complex logic has comments
- [ ] API endpoints documented

---

## Quality Assurance

### Testing Strategy for MVP

**For Newbie Teams, Focus On:**

1. **Manual Testing** (Primary)
   - Test each acceptance criterion
   - Try to break the feature
   - Test on different screen sizes

2. **Developer Testing**
   - Each developer tests their own work
   - Pair test complex features

3. **Team Lead Verification**
   - Verify all acceptance criteria
   - Test user flows end-to-end

### Test Each Story

**Before marking "Complete", test:**

1. **Happy Path:** Everything works correctly
2. **Error Cases:** Validation works
3. **Edge Cases:** Empty states, null values

**Example - Testing Story 1.2 (User Registration):**

```
Test 1: Register with valid data
✅ Result: Account created, redirected to login

Test 2: Register with existing username
✅ Result: Error message shown

Test 3: Register with short password
✅ Result: Validation error

Test 4: Leave required field empty
✅ Result: Validation error
```

---

## Issue Resolution

### Common Issues & Solutions

**Issue 1: "I don't know how to start this story"**

**Solution:**
- Break story into smaller tasks
- Pair program with another developer
- Team Lead provides example code

**Issue 2: "My code doesn't work"**

**Solution:**
- Check acceptance criteria
- Review similar completed stories
- Team Lead does code review
- Google/Spring documentation

**Issue 3: "I'm blocked by another story"**

**Solution:**
- Reorder sprint assignments
- Work on a different story
- Create a temporary stub

**Issue 4: "The requirements are unclear"**

**Solution:**
- Team Lead clarifies immediately
- Reference epics.md for acceptance criteria
- Reference architecture.md for patterns

**Issue 5: "I found a bug in completed work"**

**Solution:**
- Log the bug
- Assess severity
- Fix immediately if critical
- Otherwise, add to backlog

---

## Delivery Checklist

### Before Declaring a Story "Complete"

- [ ] All acceptance criteria pass
- [ ] Manual testing completed
- [ ] Code reviewed by Team Lead
- [ ] No console errors
- [ ] Vietnamese messages are correct
- [ ] Feature works on mobile (check responsive)
- [ ] Database changes tested

### Before Declaring Epic "Complete"

- [ ] All stories in epic are complete
- [ ] End-to-end user flow tested
- [ ] No critical bugs
- [ ] Documentation updated (if needed)

### Before Declaring MVP "Complete"

- [ ] All 4 epics complete
- [ ] Full user journey tested:
  - [ ] User registers and logs in
  - [ ] Seller creates listing
  - [ ] Admin approves listing
  - [ ] Buyer views listing
  - [ ] Buyer initiates purchase
  - [ ] Admin verifies payment
  - [ ] Seller delivers credentials
  - [ ] Buyer receives credentials
  - [ ] Admin views dashboard
- [ ] All developers have completed their assigned stories
- [ ] Code is clean and maintainable
- [ ] Ready for demo to stakeholders

---

## Quick Reference for Team Lead

### Daily Tasks

- [ ] Run daily standup (15 min)
- [ ] Update assignment tracking
- [ ] Handle blockers
- [ ] Review completed stories
- [ ] Check progress against sprint plan

### Weekly Tasks

- [ ] Plan next sprint
- [ ] Review completed work
- [ ] Demo working features
- [ ] Adjust assignments if needed

### Documents to Reference

- **epics.md** - All user stories with acceptance criteria
- **architecture.md** - Technical decisions and patterns
- **project-context.md** - Coding rules and conventions
- **prd.md** - Product requirements (if questions arise)

---

## Communication Templates

### Daily Standup Reminder

```
📢 Daily Standup starting now!

Please share:
1. What you completed yesterday
2. What you're working on today
3. Any blockers

Reply when ready!
```

### Story Assignment Message

```
📋 New Story Assigned: Story X.X - [Title]

Assigned to: @Developer
Acceptance Criteria: [Summarize key points]
Priority: [Must Have / Should Have / Nice to Have]
Due by: [Date]

Questions? Let me know!
```

### Code Review Request

```
👀 Story X.X ready for review!

Developer: @Developer
Branch/Commit: [Details]
Please test: [Key scenarios]

Ready when you are!
```

---

**Remember:** Your job is to remove blockers, provide clarity, and keep the team moving forward. Trust your developers, review their work, and celebrate completed stories!

**Good luck! 🚀**
