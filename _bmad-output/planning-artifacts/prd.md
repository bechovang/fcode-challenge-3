---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
inputDocuments:
  - product-brief-fcode project-2026-01-16.md
workflowType: 'prd'
lastStep: 10
date: 2026-01-16
author: Admin
projectName: fcode project
workflowPath: _bmad/bmm/workflows/2-plan-workflows/prd
documentCounts:
  briefCount: 1
  researchCount: 0
  brainstormingCount: 0
  projectDocsCount: 0
---

# Product Requirements Document - fcode project

**Author:** Admin
**Date:** 2026-01-16

---

## Executive Summary

**Product Name:** Game Account Shop
**Version:** 1.0 (MVP - LoL Only)
**Last Updated:** 2026-01-16
**Status:** Requirements Definition

---

### Product Vision

**Game Account Shop** is Vietnam's first trusted marketplace for buying and selling game accounts, addressing a critical trust crisis in the country's $5-10M underground gaming economy. Currently, 70% of account trades occur through unregulated Facebook groups and Discord servers, where 60-70% of participants have experienced scams or know someone who has.

Our platform solves this problem through a **trust infrastructure** that combines verified escrow payments, seller reputation systems, and buyer protection—making account trading as safe and fast as ordering on Shopee. By focusing specifically on the Vietnamese gaming market with local payment methods (VNPay, Momo), Vietnamese language support, and mobile-first design, we address gaps that global platforms like G2G fail to serve.

The product targets launch with **Liên Minh Huyền Thoại (League of Legends)** accounts—the game with Vietnam's largest player base at ~5M users—before expanding to other titles (Valorant, FIFA, Genshin Impact). Our phased approach starts with a manual concierge MVP to validate demand, then scales through automated payment integration and multi-game expansion.

**The opportunity:** Unlock currently paralyzed transactions in a market where trust barriers prevent millions of dollars in legitimate trades annually. Success means enabling thousands of Vietnamese gamers to safely trade accounts in under 30 minutes while building a platform with defensible network effects through accumulated reputation data.

---

### Problem Statement

**Vietnamese gamers lose billions of đồng annually to account trading scams on unregulated Facebook groups and Discord servers.**

The current ecosystem operates with zero trust infrastructure:
- Sellers fake screenshots and disappear after payment
- Buyers ghost after receiving account information
- Both parties face the "send money first" vs. "give account first" dilemma with no recourse
- Manual negotiations take 2-3 days with 60-70% scam rate
- No reputation system—scammers create new accounts instantly

This trust crisis creates massive transaction friction, with many gamers abandoning trades entirely due to fraud risk, and the market remains fragmented across dozens of unregulated Facebook groups with no dominant platform serving Vietnamese gamers' specific needs.

---

### Proposed Solution

**Game Account Shop** is a trust platform that enables safe, fast game account trading through three core mechanisms:

1. **Verified Escrow System:** Buyers pay through the platform; funds are held until account verification is complete. Sellers receive payment only after buyers confirm account access—eliminating "send money first" risk.

2. **Seller Reputation Infrastructure:** Every seller accumulates ratings, verified transaction count, and dispute history. This reputation becomes portable, allowing trusted sellers to charge premiums and reducing due diligence for buyers.

3. **Admin Approval Workflow:** All submitted accounts undergo admin review before listing (checking for fake screenshots, unrealistic pricing, suspicious seller patterns) to prevent fraudulent listings from reaching the marketplace.

---

### Target Market

**Primary Market:** Vietnamese gamers aged 18-30
- **Geographic Focus:** Vietnam (HCMC, Hanoi, major urban centers)
- **Initial Game:** Liên Minh Huyền Thoại (LoL) - ~5M players in Vietnam
- **Price Sweet Spot:** $5-50 USD (500k - 1.5M VNĐ) per account

**Market Size:** Estimated $5-10M annually in paralyzed transactions due to trust barriers

---

### Target Users

**Primary User: Minh (The Busy Gamer Buyer)**
- Age 24, Software Engineer in HCMC
- Pain: Lost 2M VNĐ to Facebook scam, wastes 2-3 days negotiating
- Goal: Buy verified account in <30 minutes with zero scam risk
- Success Metric: "This was faster and safer than Facebook"

**Primary User: Tuấn (The Account Farmer Seller)**
- Age 20, University Student in Hanoi
- Pain: Posts get buried, 50+ ghost inquiries, zero reputation
- Goal: Sell 3x more accounts with automatic payout and reputation premium
- Success Metric: "I stopped posting on Facebook, only sell here"

---

### Key Differentiators

**1. Local Trust Infrastructure (vs. Global Competitors):**
- Vietnamese language support and local gaming community understanding
- VNPay/Momo integration (not PayPal)
- Focus on $5-50 account sweet spot (not $100+ global market)
- Mobile-first design matching Vietnamese user behavior

**2. Reputation Moat (vs. Facebook Groups):**
- Verified seller ratings and transaction history create switching costs
- New competitors cannot replicate accumulated reputation data
- Network effects: more sellers → more buyers → more valuable platform

**3. Admin-Gated Quality (vs. Open Marketplaces):**
- All accounts reviewed before listing prevents fraud proliferation
- Higher signal-to-noise ratio than unmoderated platforms
- Creates trust premium for platform-listed accounts

**4. Phased Validation Approach (vs. Build-First Mentalities):**
- Manual concierge MVP tests demand before building complex automation
- Learning-focused: understand disputes, fraud patterns, and user needs before scaling
- Capital-efficient: prove model before significant engineering investment

**5. Platform Expansion Potential:**
- Trust infrastructure (escrow + verification + reviews) applicable to broader digital goods
- Phase 2: In-game items (skins, weapons)
- Phase 3: Boosting services
- Phase 4: General digital goods marketplace

---

### What Makes This Special

**Game Account Shop isn't just another marketplace—it's a TRUST PLATFORM that unlocks paralyzed economic activity.**

**The "Aha!" Moment:** When Minh completes a purchase in under 30 minutes and realizes "this is way better than Facebook"—and tells his gaming group.

**Network Effect:** As more sellers build reputation, more buyers join. As more buyers join, more sellers list. This flywheel creates a defensible moat that new competitors cannot replicate without years of reputation data accumulation.

**Long-Term Vision:** Become the **Shopee of Digital Goods** in Southeast Asia—the trusted platform where any digital asset can be traded safely.

---

## Project Classification

**Technical Type:** Web Application (Marketplace Platform)
**Domain:** E-Commerce / Gaming
**Complexity:** Medium
**Project Context:** Greenfield - New Project

**Technical Stack:**
- Backend: Java 17+, Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security)
- Frontend: HTML5 + CSS3 + Vanilla JavaScript, Bootstrap 5
- Database: MySQL 8.0
- Build Tool: Maven
- Payment: VNPay integration (manual MVP → automated V2)

**Architecture Pattern:** MVC + Layered Architecture (Controller → Service → Repository → Entity)

---

### Classification Rationale

**Project Type: Web Application**
- Browser-based marketplace platform
- Mobile-responsive design (not native mobile app)
- SPA/MPA considerations for user interaction
- SEO strategy for discoverability

**Domain: E-Commerce / Gaming**
- Marketplace functionality (listings, search, transactions)
- Gaming-specific account attributes (rank, champions, level)
- Payment processing and escrow
- User ratings and reputation system

**Complexity: Medium**
- Standard web application patterns (no regulatory compliance)
- Manual escrow MVP (reduces technical complexity)
- Single-game focus for MVP (LoL only)
- No real-time or streaming requirements initially
- Standard security requirements (BCrypt, session-based auth)

**Greenfield Context**
- No existing codebase to integrate with
- Full architectural freedom
- Starting from product brief, not brownfield analysis

---

## Success Criteria

### User Success

**Buyer Success Metrics (Minh's Perspective):**

**Primary Outcome Indicators:**
- **Time-to-Value:** <10 minutes from landing on site to finding qualified account
- **Transaction Completion:** >90% of initiated purchases complete successfully
- **Delivery Speed:** <30 minutes from payment to account credential delivery
- **Account Reliability:** <2% account recovery incidents at 30 days post-purchase
- **Trust Validation:** 100% of verified accounts match listing description
- **Search-to-Buy Conversion:** >25% of searches result in purchase (vs. <5% on Facebook groups)

**Behavioral Success Indicators:**
- **Repeat Purchase Rate:** >30% of buyers make second purchase within 90 days
- **Referral Rate:** >40% of buyers refer at least one friend within 60 days
- **Review Completion:** >70% of buyers leave reviews after successful transaction
- **Time from First Visit to First Purchase:** <15 minutes average (from discovery to checkout)

**Emotional Success Indicators:**
- **NPS (Net Promoter Score):** >8/10 for buyer satisfaction
- **Anxiety Reduction:** Post-purchase anxiety score drops from 8/10 (Facebook) to 3/10 (platform)
- **Trust Establishment:** >80% of first-time buyers return for second purchase

**Success Moment Definition:**
Minh achieves success when he completes a purchase in under 30 minutes, receives a working account that matches the description, and is still using that account 30 days later without any recovery issues—at which point he tells his friends "this is way better than Facebook."

---

**Seller Success Metrics (Tuấn's Perspective):**

**Primary Outcome Indicators:**
- **Listing Efficiency:** <4 hours from submission to marketplace approval
- **Sale Velocity:** Listings sell 3x faster than Facebook groups (target: <48 hours vs. 3-7 days)
- **Inquiry Quality:** >70% of messages are from qualified buyers (vs. 20% on Facebook)
- **Payment Reliability:** 100% of confirmed sales result in payout within 24 hours
- **Reputation Premium:** 5-star sellers charge 15-20% more than new sellers

**Behavioral Success Indicators:**
- **Seller Retention:** >60% of active sellers remain active after 6 months
- **Listing Volume:** Median seller lists 5+ accounts per month
- **Platform Exclusivity:** >40% of top sellers stop posting on Facebook groups
- **Multi-Game Expansion:** >30% of sellers expand to second game within 6 months

**Financial Success Indicators:**
- **Revenue Growth:** Median seller income increases 3x vs. Facebook (from ~2M to 6M VNĐ/month)
- **Commission Acceptance:** >80% of sellers accept 5-10% platform fee due to value delivered
- **Price Achievement:** >85% of listings sell at or near asking price (vs. 50% negotiation rate on Facebook)

**Success Moment Definition:**
Tuấn achieves success when he lists an account, receives 5+ qualified inquiries within 24 hours, completes a sale with zero negotiation, receives payment automatically, and earns a 5-star review that increases his account prices by 15%—at which point he tells other sellers "stop wasting time on Facebook."

---

### Business Success

**Market Validation Milestones:**

**3-Month Targets (Validation Phase):**
- **Listing Milestone:** 20+ active account listings by end of Month 1
- **Transaction Proof:** 50+ completed transactions by end of Month 3
- **User Acquisition:** 100+ registered buyers, 20+ active sellers
- **Zero-to-One:** First successful transaction within 7 days of launch
- **GMV Target:** 10M+ VNĐ in transaction value by Month 3
- **Platform Revenue:** 500k-1M VNĐ/month (5-10% commission) by Month 3

**6-Month Targets (Growth Phase):**
- **Transaction Volume:** 100+ transactions monthly (20-25/week)
- **User Base:** 500+ registered buyers, 75+ active sellers
- **Inventory Depth:** 50+ active listings at any given time
- **Repeat Business:** >30% of buyers are repeat customers
- **Monthly GMV:** 50M+ VNĐ in transaction value
- **Platform Revenue:** 3-5M VNĐ/month (5-10% commission)
- **Organic Growth:** >40% of new buyers come from referrals (not paid acquisition)

**12-Month Targets (Market Leadership):**
- **Category Leadership:** #1 platform for LoL account trading in Vietnam
- **Transaction Volume:** 500+ transactions monthly (125+ per week)
- **User Scale:** 2,000+ registered buyers, 200+ active sellers
- **Monthly GMV:** 200M+ VNĐ in transaction value
- **Platform Revenue:** 15-20M VNĐ/month (5-10% commission)
- **Multi-Game Portfolio:** LoL, Valorant, FIFA, Genshin Impact

**Business Model Validation:**
- **Unit Economics:** Positive contribution margin per transaction
- **LTV:CAC Ratio:** Target >3:1 (monitor from Month 3 onwards)
- **Commission Rate Validation:** >80% seller acceptance of 5-10% platform fee

---

### Technical Success

**Performance Requirements:**
- **System Availability:** >99% uptime during business hours (7AM - 11PM Vietnam time)
- **API Response Time:** <2 seconds for all critical user-facing APIs
- **Page Load Time:** <3 seconds for initial page load on 4G mobile connection
- **Search Performance:** <1 second for search and filter operations

**Security & Reliability:**
- **Payment Security:** 100% of VNPay transactions processed without data exposure
- **Escrow Reliability:** 100% of funds properly held in escrow and released per workflow
- **Data Integrity:** Zero tolerance for account credential data loss or leakage
- **Authentication Success:** >99% authentication success rate (valid credentials)
- **Password Security:** All passwords hashed using BCrypt with minimum 10 rounds

**Operational Capacity (Manual MVP):**
- **Manual Escrow Capacity:** System designed to handle 15-20 transactions/week with manual operations
- **Admin Review SLA:** <4 hour turnaround for account approvals during business hours
- **Support Response:** <24 hour response time for dispute resolution (email-based)
- **Capacity Acknowledgment:** At sustained 25+ transactions/week, automation investment is triggered

**Data & Privacy:**
- **User Data Privacy:** All user personal information stored securely with access controls
- **Session Management:** 30-minute session timeout with secure token generation
- **Backup Strategy:** Daily database backups with 30-day retention
- **Email Security:** Transaction emails sent through secure channels with unique tokens

**Scalability Readiness:**
- **Database Performance:** Support for 10,000+ users with responsive query times
- **File Storage:** Handle 100+ account listing screenshots with efficient serving
- **Traffic Handling:** Support 1,000+ concurrent users without performance degradation

---

### Measurable Outcomes

**North Star Metric:**

**Weekly Transaction Volume** - The single metric that measures marketplace health and liquidity

- **Definition:** Number of successfully completed buyer-seller transactions per week
- **Target:** 20 transactions/week (Month 3) → 100 transactions/week (Month 12)
- **Rationale:** Transaction volume validates demand, supply, and trust simultaneously

---

**Leading Indicators (Predict Future Success):**

**Supply-Side Health:**
- New Seller Applications: 5+/week by Month 6
- Account Submission Rate: >60% of registered sellers have active listings
- Seller Retention (30-day): >40% of sellers list second account within 30 days

**Demand-Side Health:**
- Buyer Registration Rate: 20+ new buyers/week by Month 6
- Guest-to-Registered Conversion: >25% of browsers create accounts
- Search Volume Growth: >30% month-over-month increase in marketplace searches

**Trust Health:**
- Admin Approval Rate: 60-80% (too high = lax, too low = friction)
- Pre-Transaction Inquiries: <5 average messages per listing before sale
- Dispute Rate: <5% of all transactions

---

**Lagging Indicators (Measure Past Performance):**

**Growth Metrics:**
- Monthly Active Users (MAU): Registered users who transacted in past 30 days
- New Buyer Acquisition: New buyers completing first transaction
- New Seller Activation: New sellers with first approved listing

**Engagement Metrics:**
- Buyer Repeat Rate: >30% at 90 days
- Seller Listing Frequency: 5+ listings per active seller per month
- Session Duration: Average time spent on platform per visit

**Financial Metrics:**
- Gross Merchandise Value (GMV): Total transaction value per month
- Platform Revenue: Commission income per month
- Average Transaction Value: Mean price per account sold (target: 500k-1M VNĐ)

**Trust Metrics:**
- Dispute Resolution Time: <24 hours average
- Account Recovery Incidents: <2% of accounts
- Fraud Detection Rate: >95% of fraudulent accounts blocked pre-approval
- Refund Rate: <3% of transactions

---

## Product Scope

### MVP - Minimum Viable Product

**Objective:** Validate trust infrastructure and demand in Vietnamese gaming market with minimal capital investment

**Core Features (What MUST work for this to be useful):**

**For Buyers:**
- Browse/search listings (game: LoL, filters: rank, price)
- View account details with screenshots
- View seller reputation (ratings, reviews, transaction count)
- User registration and login
- Purchase with VNPay (manual escrow process)
- Receive account credentials via platform messaging
- Leave review after successful transaction
- Basic transaction history

**For Sellers:**
- Seller registration with phone verification
- Bank account linking for payouts
- Submit account listing (game, rank, level, champions, price, screenshots)
- View listing status (Pending/Approved/Sold)
- Edit listings before approval
- Receive buyer inquiries
- Complete sale (deliver credentials after payment confirmation)
- Receive automatic payout (minus 10% commission) within 24 hours
- Accumulate public reviews and ratings

**For Admin (Trust Gatekeeper):**
- Review queue for pending account submissions
- Approve/reject listings with template responses
- Manual payment verification via VNPay dashboard
- Manual credential delivery after verification
- Process seller payouts (7-day clearing period)
- Email-based dispute resolution
- Basic dashboard (transaction counts, pending queue, recent activity)

**Platform Infrastructure:**
- User authentication (Buyer, Seller, Admin roles)
- BCrypt password hashing
- Session management (30-minute timeout)
- Database: users, game_accounts, reviews, transactions tables
- Manual VNPay QR code generation and verification
- Manual bank transfer for seller payouts
- In-platform messaging for credential delivery
- Mobile-responsive UI (Bootstrap 5)
- Vietnamese language interface

**Explicit MVP Constraints:**
- **Single Game:** Liên Minh Huyền Thoại (LoL) only
- **Manual Escrow:** All payment and credential delivery handled manually by admin
- **Basic Filtering:** Rank (Bronze → Challenger) and Price (<500k, 500k-1M, 1M+) only
- **No Real-Time Features:** No live chat, no notifications, no activity feeds
- **Email-Based Support:** Disputes handled via email, not in-platform
- **Capacity Limit:** Designed for 15-20 transactions/week maximum

**MVP Success Definition:**
MVP is considered "complete" when Minh can find a LoL account matching his criteria in <10 minutes, complete purchase with VNPay in <30 minutes, and receive working account credentials with admin verification—while seeing seller reputation and reviews that build trust.

**MVP is considered "successful" when Tuấn can submit a listing and get approved within 4 hours, receive qualified buyer inquiries (not "still available?" ghosting), complete sale and receive automatic payout within 24 hours, and accumulate 5-star reviews that increase prices by 15%.

---

### Growth Features (Post-MVP)

**Phase 2: Automation & Scale**

**Automation Investments (Trigger: Sustained 25+ transactions/week):**
- Automated VNPay payment processing with webhooks
- Automated credential delivery upon payment confirmation
- Automated seller payout scheduling (7-day clearing)
- Automated fraud detection using ML (flag suspicious patterns)

**Enhanced Features:**
- Advanced filtering: Champions, skins, server, region
- Real-time chat/messaging between buyers and sellers
- Email/SMS notifications for key events
- Watchlist and favorites functionality
- Price recommendations based on market data
- Seller analytics dashboard (views, inquiries, conversion rate)

**Multi-Game Expansion:**
- Valorant category launch
- FIFA category launch
- Genshin Impact category launch
- Cross-game search and unified seller profiles

**Infrastructure:**
- Move from shared hosting to VPS/dedicated server
- Implement caching (Redis) for performance
- CDN for static asset delivery
- Database optimization for scale

---

### Vision (Future)

**Phase 3: Platform Ecosystem (6-12 months)**

**New Product Categories:**
- In-game items trading (skins, weapons, cosmetics)
- Boosting services marketplace (pay for ranking up)
- Game currency trading
- Coaching services

**Trust Enhancements:**
- Account recovery insurance (optional buyer protection)
- Video verification for high-value accounts
- Verified seller badge system (ID verification, video intro)
- Dispute resolution center with UI (replacing email)

**Social Features:**
- Seller profiles with customization
- User-generated content (guides, reviews, tutorials)
- Community features (forums, groups by game)
- Referral/affiliate program

**Geographic Expansion:**
- Thailand market entry
- Philippines market entry
- Localized payment methods for each country
- Multi-language support (Thai, Filipino, English)

**Strategic Partnerships:**
- Gaming community partnerships (streamers, teams, Discord servers)
- Game publisher partnerships (if/when account selling becomes accepted)
- Payment processor partnerships (beyond VNPay)

---

**Phase 4: Digital Goods Platform (12-24 months)**

**Beyond Gaming:**
- Courses and ebooks marketplace
- Software license trading
- Digital art and NFT marketplace
- Services marketplace (freelance digital services)

**Platform Capabilities:**
- API for third-party integrations
- Mobile apps (iOS, Android)
- Advanced analytics and BI tools
- Machine learning pricing recommendations
- Automated dispute resolution with AI mediation

**Business Model Evolution:**
- Subscription tiers for sellers (premium placement, analytics)
- Buyer protection insurance as revenue stream
- White-label platform for other markets
- B2B enterprise solutions (game studios, publishers)

**North Star Vision:**
Become the **Shopee of Digital Goods** in Southeast Asia - the trusted platform where any digital asset can be traded safely, with Game Account Shop as the foundational vertical that established trust, infrastructure, and market position.

---

## User Journeys

### Journey 1: Minh - The First Safe Purchase

**Opening Scene: Friday Evening, 9:30 PM**

Minh, a 24-year-old software engineer in HCMC, just got a call from his college friends. They want to play League of Legends tonight—it's been months since they've all been online together. But there's a problem: Minh's old account was banned last month after a dispute with a toxic teammate, and he doesn't have time to grind a new account to level 30.

He remembers seeing a post in a gaming Facebook group about "Game Account Shop"—something about safe trading. He's skeptical. Last time he tried buying an account on Facebook, he lost 2M VNĐ when the seller ghosted after payment. But his friends are waiting, and he's desperate.

**Rising Action: Discovery and Trust Building**

Minh opens the site on his phone. The interface is clean, Vietnamese language everywhere—this feels different from the sketchy sites he's seen before. He taps the search bar and selects "Liên Minh Huyen Thoại" from the game dropdown.

He filters: "Gold IV or higher" and price range "500k - 1M VNĐ." Three listings appear.

The first listing catches his eye: Gold III, 80 champions, 3 skins—priced at 800k VNĐ. But what makes him pause is the seller profile: "TuấnStore" with 47 reviews, averaging 4.9 stars. The reviews are detailed: "Received account in 15 minutes," "Exactly as described," "Third purchase, always reliable."

Minh clicks "View Details." The listing shows screenshots of the account's champion roster, ranked stats, and skin inventory. The description mentions the account was played fair-and-square, no boosting, no hacks. At the bottom, a badge: "Verified Seller" with a green checkmark.

**Climax: The Transaction**

Minh creates an account in under a minute—just username, password, and phone number. He's taken to a VNPay QR code screen. The price is 800k VNĐ plus 80k VNĐ platform fee (10%). He scans with his VNPay app and confirms payment.

A screen appears: "Payment received. Your order is being verified. You will receive account credentials within 30 minutes or your money back."

Minh waits. He refreshes the page. At 9:48 PM—18 minutes later—a notification appears: "Account verified. Credentials delivered."

He opens the in-platform message. There's the username and password, along with setup instructions and a warning: "Please change password immediately after login."

**Resolution: Trust Established**

Minh logs into the LoL client. It works. He's in. He changes the password, adds his email for recovery, and joins his friends' lobby. They play until 2 AM.

Three days later, he gets an email from Game Account Shop: "How was your experience? Please leave a review for TuấnStore." Minh logs in, gives 5 stars, and writes: "Fast, safe, way better than Facebook. Highly recommend."

**30 Days Later:** The account still works. No recovery issues. Minh has recommended the platform to three friends. He's now a regular browser, checking for smurf accounts whenever he wants to try new champions.

---

### Journey 2: Tuấn - From Buried Posts to Reputation Premium

**Opening Scene: Frustrated Sunday Afternoon**

Tuấn, a 20-year-old university student in Hanoi, has been selling LoL accounts for extra income since high school. He's good at it—he can level an account to 30 with decent champions in about two weeks of casual play. But selling is the hard part.

He's currently sitting at his laptop, frustrated. He's posted three accounts on three different Facebook groups in the past week. Two posts are buried under dozens of newer listings. The third post has 15 comments—all "still available?" with zero follow-up.

Tuấn opens his messages: 47 notifications from potential buyers. He knows from experience that at least 35 of these are ghosts—people who ask "still avail?" and never respond again. Of the remaining 12, maybe two will actually negotiate, and one will try to lowball him by half.

He's averaging 1-2 sales per month, spending 10+ hours weekly on negotiations and ghost-chasing. His total monthly income: barely 2M VNĐ.

**Rising Action: Discovery and First Sale**

Tuấn's roommate tells him about Game Account Shop. "It's like Shopee for gaming accounts. You list, they verify, you sell. No more ghost messages." Tuấn is skeptical but decides to try.

He registers as a seller—a slightly longer process. He needs to provide his phone number, link his bank account for payouts, and verify his identity with a photo of his student ID. "We need to know you're a real person," the onscreen message explains.

Tuấn submits his first listing: Silver II account, 60 champions, 5 rare skins—priced at 650k VNĐ. He uploads screenshots of the account, writes a detailed description, and submits.

Two hours later, he gets an email: "Your listing has been approved and is now live on the marketplace."

The same evening, he gets his first inquiry through the platform: "Is this still available?" Tuấn rolls his eyes—another ghost? But then he notices something different: The buyer has already registered and verified his phone number. The platform shows "Verified Buyer" badge next to the message.

Tuấn responds: "Yes, available." The buyer replies: "I'll take it. Processing payment now."

**Climax: The First Platform Sale**

Tuấn watches in disbelief as the transaction status updates:
- "Payment received from buyer"
- "Admin verification in progress"
- "Payment verified. Please deliver account credentials."

Tuấn copies the username and password into the platform's secure message system. The buyer receives them, logs in, and confirms receipt.

One hour later, Tuấn gets a notification: "Sale completed. Commission: 65k VNĐ. Net payout: 585k VNĐ. Your bank account ending in 4721 will receive funds within 24 hours."

The next day, 585k VNĐ appears in Tuấn's bank account. No negotiations. No payment chasing. No ghosting.

**Resolution: Reputation and Scaling**

The buyer leaves a 5-star review: "Fast delivery, account exactly as described. Thanks TuấnStore!" This is Tuấn's first public review on the platform.

Emboldened, Tuấn lists two more accounts the following week. Both sell within 48 hours. Within a month, he's listed 8 accounts and sold 6—a 75% success rate, compared to his 20% rate on Facebook.

**Month 2:** Tuấn's seller profile now shows 12 reviews, averaging 4.9 stars. He experiments with pricing—raising his listed prices by 15%. They still sell. Buyers are paying a premium for his reputation.

**Month 3:** Tuấn earns 6M VNĐ from account sales—3x his Facebook income. He stops posting on Facebook groups entirely. He tells his friends: "Đừng bán trên Facebook nữa, vào đây bán cho nhanh." (Don't sell on Facebook anymore, sell here for faster results.)

**6 Months Later:** Tuấn is one of the platform's top 10 sellers. He's hired two part-time helpers to level accounts. He's selling accounts for Valorant and FIFA too. He's earning 10M+ VNĐ monthly, and he's proud of his 4.9-star reputation across 80+ sales.

---

### Journey 3: Admin - Guardian of Trust

**Opening Scene: Morning Review Queue**

The admin (let's call her Lan) logs into her dashboard at 9 AM. She sees the overnight activity:

- 8 new account submissions pending review
- 3 completed transactions requiring payout processing
- 2 buyer inquiries about account details
- 1 dispute ticket submitted via email

Lan starts with the review queue. Her job is critical: she's the gatekeeper preventing fraudulent accounts from reaching the marketplace.

**Rising Action: Review and Verification**

Listing #1: A Platinum III account with 120 champions, priced at 2.5M VNĐ. Lan reviews the screenshots—champion roster looks legitimate, ranked stats match the claimed rank, skins are realistic for the account's level. She checks the seller's profile: "GamingPro1999" with 15 prior sales and 4.8-star average. No red flags. **APPROVE.**

Listing #2: A Diamond V account with all champions unlocked, priced at 800k VNĐ. Lan's eyebrows go up. This is way too cheap for a Diamond account with full champion roster. The screenshots look generic—possibly stolen from another listing. The seller is new with zero history. Lan checks the seller's registration: created 2 hours ago. **REJECT** with template message: "Your listing was declined due to suspicious pricing. Please provide additional verification or adjust pricing to market rates."

Listing #3: A Gold II account with 40 champions, priced at 550k VNĐ. Screenshots look legitimate, but the seller included a phone number in the description: "Contact me on Zalo for faster deal." This is a violation—platform rules prohibit off-platform transactions to prevent fraud. Lan edits the description to remove the phone number and **APPROVES** with a warning message to the seller.

**Climax: Transaction Escrow**

After processing 5 more listings (4 approve, 1 reject), Lan moves to the completed transactions. Three buyers paid overnight, and she needs to verify payments and facilitate credential delivery.

Transaction #1: Buyer paid 700k VNĐ for a Gold IV account. Lan opens the VNPay dashboard, confirms payment received, and marks the transaction as "Payment Verified." She sends an automated message to the seller: "Buyer has paid. Please deliver account credentials within 2 hours."

The seller responds in 45 minutes with the credentials. Lan reviews them, then forwards to the buyer with instructions: "Please log in and change password immediately. Confirm receipt within 24 hours."

Two hours later, the buyer confirms: "Account received, works perfectly." Lan updates the transaction status to "COMPLETED" and schedules the seller's payout (minus 10% commission) for 7 days later—per platform policy to allow for buyer disputes.

**Resolution: Day's End**

At 6 PM, Lan wraps up her day. She's processed:
- 8 account submissions (6 approved, 2 rejected)
- 15 completed transactions (100% success rate, 0 disputes)
- 23 buyer inquiries
- 1 dispute ticket (resolved in 2 hours)

She checks the daily stats dashboard:
- 24 transactions completed today (new record)
- 1.2M VNĐ in platform revenue (10% commission)
- Zero fraud incidents detected
- Buyer satisfaction: 4.8/5 average from today's reviews

Lan logs off, satisfied. The system is working. Trust is being maintained.

---

### Journey 4: Minh - The Dispute Resolution (Edge Case)

**Opening Scene: The Recovery Incident**

It's been 45 days since Minh bought his account from TuấnStore. He's logged in daily, ranked up from Gold IV to Gold II, and even purchased a few skins with real money. Life is good.

Until one evening, Minh tries to log in and gets the error: "Incorrect username or password."

He tries again. Same error. He resets his password via email. Still can't log in. Panic sets in. Did he get hacked? Did the original seller recover the account?

**Rising Action: Dispute Filing**

Minh goes to Game Account Shop and navigates to "My Purchases." He finds the transaction and clicks "Report Issue." A form appears:

**Type of Issue:**
- Account not working
- Account recovered by seller
- Account doesn't match description
- Other: _______

Minh selects "Account recovered by seller" and submits details: "I can't log in anymore. Password reset doesn't work. I think the seller recovered it."

An automated response appears: "Your dispute has been submitted. Our team will investigate within 24 hours. Please save all communications and screenshots."

**Climax: Investigation and Resolution**

The next morning, Lan (the admin) receives Minh's dispute ticket via email. She logs into the admin panel and reviews the transaction:

- Buyer: Minh (verified, 3 prior purchases, 5-star review history)
- Seller: TuấnStore (47 reviews, 4.9-star average, top 10 seller)
- Transaction: 45 days old, completed successfully
- No prior disputes from either party

This doesn't look like fraud—TuấnStore is a reputable seller with zero dispute history. Lan messages TuấnStore: "Buyer reports account recovery. Please check your records."

Tuấn responds within an hour: "I haven't recovered any accounts. Let me check if this was an account I bought from someone else."

Lan investigates further. She discovers the account Tuấn sold to Minh was originally sourced from another seller (let's call him "NewGuy2024") who has since been banned from the platform for fraud. It appears "NewGuy2024" recovered the account using his original email—which he hadn't properly transferred to Tuấn.

**Resolution: Full Refund and Seller Accountability**

Lan makes a decision: This is a platform failure. The admin approval process didn't catch that "NewGuy2024" had retained recovery email access. She messages Minh:

"We've investigated and confirmed this was a legacy account from a now-banned seller who retained recovery access. This is our fault for not catching this earlier. You'll receive a full refund of 880k VNĐ (including platform fee) within 24 hours. We've also permanently banned the problematic seller and implemented additional verification checks for account sourcing."

Minh receives the refund the next day. He's impressed: "I expected to lose my money. Thank you for standing behind the purchase."

He buys another account the following week—from TuấnStore again, who offers him a 10% discount "for the trouble."

**Outcome:** Minh's trust in the platform actually INCREASES after the dispute. He tells his friends: "Even when something goes wrong, they fix it. Way better than Facebook."

---

### Journey Requirements Summary

**From Journey 1 (Minh - Purchase):**
- Browse/search with filters (rank, price)
- Seller reputation display (ratings, reviews, transaction count)
- User registration and login
- VNPay payment integration
- Payment verification status tracking
- Secure credential delivery
- Post-purchase review system
- Transaction history

**From Journey 2 (Tuấn - Seller):**
- Seller registration with identity verification
- Bank account linking for payouts
- Account listing submission with screenshots
- Listing status tracking (Pending/Approved/Sold)
- In-platform messaging (buyer inquiries)
- Automatic payout calculation (commission deduction)
- Public seller profile and reviews
- Commission structure (5-10%)
- 7-day payout clearing period

**From Journey 3 (Admin - Operations):**
- Admin review queue for pending listings
- Approve/reject functionality with template messages
- VNPay payment verification dashboard
- Transaction escrow management
- Manual credential delivery coordination
- Payout scheduling
- Email-based dispute resolution
- Admin dashboard (stats, activity log)
- Fraud detection capabilities

**From Journey 4 (Minh - Dispute):**
- Dispute filing system
- Dispute investigation workflow
- Refund processing capability
- Seller banning capability
- Platform communication during disputes
- Resolution tracking

---

## Web Application Specific Requirements

### Project-Type Overview

**Game Account Shop** is a **Multi-Page Application (MPA)** built with Java Spring Boot backend and traditional server-side rendering. The application serves the Vietnamese gaming market with a mobile-first responsive design, prioritizing modern browser support (Chrome, Safari) over legacy compatibility.

### Technical Architecture Considerations

**Application Pattern:** Multi-Page Application (MPA)
- Server-side rendering with Spring Boot MVC controllers
- Form-based submissions with page refreshes
- Minimal JavaScript for UI enhancements (Bootstrap 5 components)
- Traditional HTTP request/response cycle

**Rationale for MPA:**
- Aligns with Java web system educational context
- Simpler debugging and development for NEWBIE developers
- Better SEO out-of-the-box compared to SPA
- Sufficient for manual escrow workflow (no real-time requirements)

### Browser Support Matrix

**Primary Browsers (Full Support):**
- **Chrome 90+** (Windows, Android, macOS) - 75%+ Vietnam market share
- **Safari 14+** (iOS, macOS) - Critical for iPhone users

**Secondary Browsers (Best Effort):**
- **Samsung Internet 14+** (Android) - Significant Android share
- **Edge 90+** (Chromium-based) - Fallback for Windows users

**Excluded:**
- Internet Explorer (any version) - No support required
- Firefox - Test if possible, but not primary target
- Opera Mini - Limited CSS/JS support

**Mobile vs Desktop:**
- **60-70% mobile traffic expected** (Vietnam market pattern)
- **30-40% desktop traffic**
- Mobile-first design approach prioritized

### Responsive Design Strategy

**Breakpoint Strategy (Bootstrap 5):**
- **xs (< 576px):** Mobile-first baseline - single column, stacked layout
- **sm (≥ 576px):** Large phones - 2-column grid for listings
- **md (≥ 768px):** Tablets - Sidebar + main content layout
- **lg (≥ 992px):** Small desktops - 3-column listing grid
- **xl (≥ 1200px):** Large desktops - 4-column listing grid + enhanced navigation

**Mobile-First Design Principles:**
- Touch-friendly tap targets (minimum 44x44px)
- Simplified navigation with hamburger menu
- Bottom navigation bar for key actions (Browse, Sell, Profile)
- Opt-in advanced features (don't clutter mobile interface)
- VNPay QR code display optimized for mobile scanning

**Content Prioritization:**
- Mobile: Search → Top Listings → Quick Filters
- Desktop: Enhanced filters → Sidebar → Advanced sorting

### Performance Targets

**Page Load Performance:**
- **Time to First Byte (TTFB):** < 500ms (server response)
- **First Contentful Paint (FCP):** < 1.5s on 4G mobile
- **Largest Contentful Paint (LCP):** < 2.5s on 4G mobile
- **Time to Interactive (TTI):** < 3.5s on 4G mobile

**Resource Optimization:**
- Compress images (account screenshots) before upload
- Lazy load listing images below the fold
- Minify CSS and JavaScript in production
- Enable gzip compression on server responses
- CDN for static assets (Bootstrap CSS/JS) via public CDNs

**Database Query Performance:**
- All listing queries < 500ms
- Search operations with filters < 1s
- User authentication < 200ms
- Pagination for large result sets (20 listings per page)

### SEO Strategy

**Keyword Targets (Vietnamese):**
- "mua bán tài khoản game" (buy sell game accounts)
- "mua acc LMHT" (buy LoL account)
- "bán acc uy tín" (sell reputable account)
- "tài khoản LMHT giá rẻ" (LoL account cheap)
- "mua account vàng" (buy gold account)

**On-Page SEO Requirements:**
- **Title Tags:** Dynamic titles per page (e.g., "Mua tài khoản LMHT Gold III - Game Account Shop")
- **Meta Descriptions:** Unique descriptions for listing pages (150-160 characters)
- **Semantic HTML:** Proper use of `<h1>`, `<h2>`, `<article>`, `<section>` tags
- **Image Alt Text:** Descriptive alt text for account screenshots
- **URL Structure:** Clean, keyword-inclusive URLs
  - `/tai-khoan/lol/gold-iii` (LoL Gold III accounts)
  - `/tai-khoan/lol/cao-cap` (LoL premium accounts)

**Sitemap & Indexing:**
- **XML Sitemap:** Auto-generated for all approved listings
- **Robots.txt:** Allow crawling of approved listings, block admin/checkout pages
- **Canonical URLs:** Prevent duplicate content issues
- **Schema Markup:** Product schema for account listings (price, availability, reviews)

**Technical SEO:**
- SSL certificate (HTTPS) - Required for trust
- Mobile-friendly design (responsive)
- Fast page load times (< 3s)
- Clean URL structure (no query parameters for listing pages)

### Accessibility Level

**Target Compliance:** WCAG 2.1 Level AA

**Key Accessibility Features:**

**Visual Accessibility:**
- Color contrast ratio ≥ 4.5:1 for normal text, ≥ 3:1 for large text
- No reliance on color alone to convey information (use icons + text)
- Scalable text (up to 200% zoom) without horizontal scrolling
- Clear focus indicators for keyboard navigation

**Keyboard Accessibility:**
- All interactive elements accessible via keyboard (Tab, Enter, Space)
- Logical tab order (left-to-right, top-to-bottom)
- Skip navigation link for screen readers
- No keyboard traps

**Screen Reader Support:**
- Semantic HTML structure (nav, main, article, footer)
- ARIA labels for interactive elements (buttons, forms)
- Alt text for all images (account screenshots, icons)
- Form labels associated with inputs (not just placeholders)

**Form Accessibility:**
- Clear error messages linked to form fields
- Required fields marked explicitly (not just color)
- Input validation with descriptive feedback
- Sufficient time to complete forms (no timeout on listing submission)

**Testing:**
- Test with NVDA screen reader (Windows)
- Test with VoiceOver (iOS/macOS)
- Keyboard-only navigation testing
- Color blindness simulation testing

### Implementation Considerations

**Development Workflow:**
- Use Spring Boot DevTools for hot reload during development
- Test on multiple browsers using BrowserStack or local testing
- Progressive enhancement approach (core functionality works without JavaScript)
- Graceful degradation for unsupported browsers

**Deployment Considerations:**
- Enable gzip compression on Spring Boot server
- Configure proper cache headers for static assets
- Use environment variables for sensitive configuration (API keys, database credentials)
- Set up proper error pages (404, 500) with helpful guidance

**Monitoring & Analytics:**
- Google Analytics 4 integration for traffic tracking
- Server-side logging for errors and performance monitoring
- Database query performance monitoring
- Mobile vs desktop usage tracking

---

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Experience MVP + Platform Foundation

Deliver the complete "safe purchase" experience (browse → pay → receive verified account) with working escrow, while building the reputation system foundation from day one as a critical competitive moat.

**Resource Requirements:**
- **Team:** 1 Full-stack developer (Java Spring Boot + HTML/CSS/JS) + 1 Part-time admin
- **Timeline:** 2-3 months to MVP launch
- **Capacity:** Designed for 15-20 transactions/week with manual operations

### MVP Feature Set (Phase 1: Months 1-3)

**Core User Journeys Supported:**
1. Buyer: Browse listings → Find account → Pay with VNPay → Receive credentials
2. Seller: Register → Submit listing → Get approved → Deliver credentials → Get paid
3. Admin: Review queue → Approve listings → Verify payments → Process payouts

**Must-Have Capabilities:**
- User authentication (Buyer, Seller, Admin roles)
- Account listing submission with screenshots
- Admin approval/reject workflow
- VNPay QR code generation for payments
- Manual payment verification and credential delivery
- Manual bank transfer payouts (10% commission)
- Basic search and filtering (rank, price range)
- Seller reputation display (ratings, reviews, transaction count)
- Email-based dispute resolution
- Basic admin dashboard (pending queue, transaction stats)

**Explicit MVP Constraints:**
- Single game: LoL only
- Manual escrow (all payment/credential operations manual)
- 15-20 transactions/week capacity
- Email-based communication (no in-platform messaging)
- Capacity acknowledgment: At 25+ transactions/week, automation investment triggers

### Post-MVP Features

**Phase 2: Growth (Months 4-6)**
*Trigger: Sustained 25+ transactions/week*

- Public seller profiles with detailed history
- In-platform messaging system
- Transaction history for buyers and sellers
- Advanced filtering (champions, skins, server)
- Watchlist/favorites functionality
- Automated VNPay payment processing with webhooks
- Automated credential delivery upon payment confirmation
- Automated payout scheduling (7-day clearing period)
- Seller analytics dashboard (views, inquiries, conversion rate)

**Phase 3: Expansion (Months 7-12)**
*Trigger: 100+ transactions/week*

- Multi-game support (Valorant, FIFA, Genshin Impact)
- Automated fraud detection using ML
- Video verification for high-value accounts
- Dispute resolution center with UI (replace email-based)
- Verified seller badge system (ID verification, video intro)
- Account recovery insurance (optional buyer protection)
- Referral/affiliate program
- Cross-game search and unified seller profiles

**Phase 4: Platform Evolution (Year 2+)**

- New product categories (in-game items, boosting services, game currency)
- Geographic expansion (Thailand, Philippines)
- Localized payment methods for new markets

### Risk Mitigation Strategy

**Technical Risks:**
- **VNPay integration complexity:** Start with manual QR code generation; build abstraction layer for payment processing; have Momo as backup
- **Manual operations scaling:** Design automation hooks from day 1; monitor volume weekly; trigger automation at 25 transactions/week

**Market Risks:**
- **Low user trust:** Admin approval for ALL listings; first purchase guarantee; seller reputation as key differentiator; start with low-risk accounts (500k-1M VNĐ)
- **Game publisher crackdown:** Clear "at your own risk" disclaimer; don't advertise on official game channels; have contingency plan for other games

**Resource Risks:**
- **Limited team capacity:** MVP designed for 1-2 developers; manual operations reduce complexity; use Bootstrap 5 for UI
- **Contingency:** Absolute minimum 1.5 FTE (1 full-stack dev + 0.5 admin)

---

## Functional Requirements

### User Account Management

- **FR-001:** Guest users can register a new account with username, password, email, and full name
- **FR-002:** Users can log in with username and password
- **FR-003:** Users can log out and terminate their session
- **FR-004:** Users can update their personal profile information
- **FR-005:** Users can change their password
- **FR-006:** System assigns one of three roles to each user (Buyer, Seller, Admin)
- **FR-007:** System enforces session timeout after 30 minutes of inactivity
- **FR-008:** Sellers can verify their identity through document submission
- **FR-009:** Sellers can link bank account information for payouts
- **FR-010:** Admin users can view all registered users

### Account Listing Management

- **FR-011:** Sellers can submit new game account listings with game name, rank, level, champions, items, and price
- **FR-012:** Sellers can upload screenshots to accompany account listings
- **FR-013:** Sellers can view the status of their submitted listings
- **FR-014:** Sellers can edit their listings before admin approval
- **FR-015:** System sets new listings to "Pending" status upon submission
- **FR-016:** Admin users can view all pending listings in a review queue
- **FR-017:** Admin users can approve pending listings
- **FR-018:** Admin users can reject pending listings with a reason
- **FR-019:** System updates listing status to "Approved" when approved by admin
- **FR-020:** System updates listing status to "Rejected" when rejected by admin
- **FR-021:** System updates listing status to "Sold" when purchased

### Marketplace Discovery & Search

- **FR-022:** Guest users and buyers can browse all approved listings
- **FR-023:** Users can search listings by game name
- **FR-024:** Users can filter listings by rank (Bronze through Challenger)
- **FR-025:** Users can filter listings by price range
- **FR-026:** Users can view detailed information for a specific listing
- **FR-027:** Users can view seller reputation information on listings
- **FR-028:** System displays seller ratings, review count, and transaction count

### Transaction & Escrow

- **FR-029:** Buyers can initiate purchase of a specific listing
- **FR-030:** System generates a VNPay QR code for payment
- **FR-031:** System displays payment instructions including total price and platform fee
- **FR-032:** Admin users can verify VNPay payment receipt
- **FR-033:** System updates transaction status when payment is verified
- **FR-034:** Sellers can deliver account credentials after payment verification
- **FR-035:** System can deliver account credentials to buyers after admin verification
- **FR-036:** System stores buyer confirmation of credential receipt
- **FR-037:** System calculates and deducts platform commission (10%) from transaction amount
- **FR-038:** Admin users can process manual bank transfer payouts to sellers
- **FR-039:** System enforces a 7-day clearing period before seller payouts

### Trust & Reputation System

- **FR-040:** Buyers can submit reviews and ratings for completed transactions
- **FR-041:** System displays seller profiles with cumulative ratings
- **FR-042:** System displays seller transaction count
- **FR-043:** System displays individual buyer reviews for each seller
- **FR-044:** System calculates and maintains average seller ratings
- **FR-045:** System awards "Verified Seller" badge to sellers meeting criteria

### Dispute Resolution

- **FR-046:** Buyers can submit disputes for purchased accounts
- **FR-047:** Users can select dispute type from predefined options
- **FR-048:** Users can provide detailed description and evidence for disputes
- **FR-049:** Admin users can investigate submitted disputes
- **FR-050:** Admin users can communicate with users regarding disputes
- **FR-051:** Admin users can approve full refunds for disputes
- **FR-052:** Admin users can ban sellers for fraud or policy violations
- **FR-053:** System sends email notifications for dispute status updates

### Admin Operations & Oversight

- **FR-054:** Admin users can access a dashboard with platform statistics
- **FR-055:** System displays total transaction count on admin dashboard
- **FR-056:** System displays pending review queue count on admin dashboard
- **FR-057:** System displays recent activity log on admin dashboard
- **FR-058:** Admin users can view transaction history for any user
- **FR-059:** Admin users can flag suspicious listings or accounts
- **FR-060:** Admin users can edit listing descriptions to remove policy violations

### Platform Communication

- **FR-061:** System sends email notifications for listing approval status
- **FR-062:** System sends email notifications when buyers receive credentials
- **FR-063:** System sends email requests for buyers to leave reviews
- **FR-064:** System sends email notifications for dispute responses
- **FR-065:** Users can receive platform announcements via email

### Platform Experience (SEO, Accessibility, Responsive)

- **FR-066:** System displays responsive layouts for mobile, tablet, and desktop screen sizes
- **FR-067:** System supports keyboard navigation for all interactive elements
- **FR-068:** System provides semantic HTML structure for screen readers
- **FR-069:** System displays text with WCAG 2.1 Level AA compliant color contrast
- **FR-070:** System generates SEO-friendly URLs for listing pages
- **FR-071:** System displays dynamic page titles for listing pages
- **FR-072:** System displays meta descriptions for listing pages

---

## Non-Functional Requirements

### Performance

**Page Load Performance:**
- **NFR-001:** Initial page load completes within 3 seconds on 4G mobile connection
- **NFR-002:** Time to First Byte (TTFB) is less than 500ms for all pages
- **NFR-003:** Search and filter operations complete within 1 second
- **NFR-004:** User authentication completes within 200ms for valid credentials

**Database Performance:**
- **NFR-005:** Listing queries return results within 500ms
- **NFR-006:** User profile queries complete within 300ms
- **NFR-007:** Transaction history queries support pagination without performance degradation

**Resource Optimization:**
- **NFR-008:** Static assets (CSS, JS, images) are served with caching headers
- **NFR-009:** Listing images are compressed before storage
- **NFR-010:** Server responses use gzip compression for text-based content

### Security

**Authentication & Authorization:**
- **NFR-011:** All user passwords are hashed using BCrypt with minimum 10 rounds
- **NFR-012:** User sessions expire after 30 minutes of inactivity
- **NFR-013:** System enforces role-based access control (Buyer, Seller, Admin)
- **NFR-014:** Admin actions are logged with timestamp and user identifier

**Data Protection:**
- **NFR-015:** User personal information is stored securely with access controls
- **NFR-016:** Account credentials are encrypted at rest in the database
- **NFR-017:** Bank account information is encrypted and never displayed in full
- **NFR-018:** All communication over HTTPS/TLS 1.3 or higher

**Payment Security:**
- **NFR-019:** VNPay integration follows PCI-DSS compliance guidelines
- **NFR-020:** Payment transaction data is never stored in plaintext
- **NFR-021:** Platform fee calculations are auditable and tamper-proof

**Input Validation:**
- **NFR-022:** All user inputs are sanitized to prevent SQL injection
- **NFR-023:** File uploads are validated for type and size before storage
- **NFR-024:** Cross-Site Scripting (XSS) protection is implemented for all user-generated content

### Scalability

**User Capacity:**
- **NFR-025:** System supports 1,000+ concurrent users without performance degradation
- **NFR-026:** Database performance remains responsive with 10,000+ registered users
- **NFR-027:** System handles 100+ concurrent listing views without slowdown

**Transaction Volume:**
- **NFR-028:** System supports 15-20 transactions/week in MVP configuration
- **NFR-029:** System architecture supports scaling to 100+ transactions/week with automation
- **NFR-030:** Database queries remain efficient as listing count grows to 1,000+

**Storage Capacity:**
- **NFR-031:** System supports storage for 100+ listing screenshots
- **NFR-032:** Database supports growth to 10,000+ transaction records
- **NFR-033:** File storage system supports efficient serving of images

### Accessibility

**WCAG 2.1 Level AA Compliance:**
- **NFR-034:** Text color contrast meets minimum 4.5:1 ratio for normal text
- **NFR-035:** All interactive elements are keyboard accessible
- **NFR-036:** Semantic HTML structure supports screen reader navigation
- **NFR-037:** Focus indicators are visible for all interactive elements
- **NFR-038:** Form inputs have associated labels (not placeholder-only)
- **NFR-039:** Error messages are clearly associated with form fields
- **NFR-040:** Images have descriptive alt text (especially for listing screenshots)

**Responsive Design:**
- **NFR-041:** Interface functions properly on mobile devices (60-70% of traffic)
- **NFR-042:** Touch targets are minimum 44x44 pixels for mobile interaction
- **NFR-043:** Content is readable without horizontal zoom on mobile devices

### Integration

**VNPay Payment Gateway:**
- **NFR-044:** System generates valid VNPay QR codes for payments
- **NFR-045:** Payment verification process completes within manual processing SLA
- **NFR-046:** System handles VNPay API errors gracefully with user-friendly messages
- **NFR-047:** Payment status updates are reflected in transaction records

**Email Service:**
- **NFR-048:** System sends transactional emails within 5 minutes of triggering events
- **NFR-049:** Email templates are localized in Vietnamese language
- **NFR-050:** System handles email delivery failures with retry logic
- **NFR-051:** Email content includes secure tokens for verification links

**Future Integration Readiness:**
- **NFR-052:** Payment system abstraction allows adding Momo or other providers
- **NFR-053:** Email service abstraction allows switching providers
- **NFR-054:** API design supports future webhook integrations for automation

### Reliability

**System Availability:**
- **NFR-055:** Platform maintains 99% uptime during business hours (7AM - 11PM Vietnam time)
- **NFR-056:** Planned maintenance is announced at least 24 hours in advance
- **NFR-057:** System implements graceful error handling to prevent complete service failure

**Data Integrity:**
- **NFR-058:** Database transactions follow ACID principles
- **NFR-059:** System implements daily automated backups with 30-day retention
- **NFR-060:** Critical data (transactions, payouts) is never lost due to system errors

**Error Handling:**
- **NFR-061:** User-facing errors display clear, actionable messages in Vietnamese
- **NFR-062:** System logs all errors with sufficient context for troubleshooting
- **NFR-063:** System prevents data corruption through transaction rollbacks on errors

---
