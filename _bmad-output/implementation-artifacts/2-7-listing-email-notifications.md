# Story 2.7: Listing Email Notifications

Status: todo

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **seller**,
I want **to receive email notifications when my listing is approved or rejected**,
So that **I know the status of my listing without checking the website**.

## Acceptance Criteria

**Given** I am a seller who created a listing
**When** an admin approves my listing
**Then** I receive an email with subject "✅ Listing của bạn đã được duyệt!"
**And** the email contains:
  - Game name
  - Rank
  - Price
  - A message that my listing is now visible on the homepage
  - Link to view the listing

**Given** I am a seller who created a listing
**When** an admin rejects my listing
**Then** I receive an email with subject "❌ Listing của bạn đã bị từ chối"
**And** the email contains:
  - Game name
  - Rank
  - Price
  - The rejection reason
  - A message to fix and resubmit
  - Contact info for support

**Given** the email fails to send
**When** the admin approves or rejects a listing
**Then** an error message is displayed on the admin dashboard
**And** the admin is notified that the email was not delivered
**And** the listing status is still updated (email failure doesn't block the action)

**Technical Notes:**
- Use JavaMail API with Gmail SMTP
- Email config in application.yml:
  ```yaml
  spring.mail.host: smtp.gmail.com
  spring.mail.port: 587
  spring.mail.username: your-email@gmail.com
  spring.mail.password: xxxx-xxxx-xxxx-xxxx
  spring.mail.properties.mail.smtp.auth: true
  spring.mail.properties.mail.smtp.starttls.enable: true
  ```
- Send emails asynchronously to avoid blocking the approval/rejection action
- Handle email exceptions gracefully (log error, show dashboard notification)
- HTML email templates for better formatting

## Tasks / Subtasks

- [ ] Add Spring Boot Mail dependency (AC: All)
  - [ ] Add spring-boot-starter-mail to pom.xml
  - [ ] Configure Gmail SMTP in application.yml

- [ ] Create EmailService (AC: #1, #2)
  - [ ] Create sendListingApprovedEmail method
  - [ ] Create sendListingRejectedEmail method
  - [ ] Use JavaMail API with Gmail SMTP
  - [ ] Send HTML formatted emails
  - [ ] Handle exceptions gracefully

- [ ] Create email templates (AC: #1, #2)
  - [ ] Create approval email template (HTML)
  - [ ] Create rejection email template (HTML)
  - [ ] Use Vietnamese language

- [ ] Update GameAccountService (AC: #1, #2, #3)
  - [ ] Inject EmailService
  - [ ] Send approval email when status changes to APPROVED
  - [ ] Send rejection email when status changes to REJECTED
  - [ ] Handle email failures without blocking status update
  - [ ] Log email delivery status

- [ ] Update AdminController (Story 2.4) (AC: #1, #2, #3)
  - [ ] Show success/error messages for email delivery
  - [ ] Log email failures for admin visibility
  - [ ] Ensure listing status updates even if email fails

- [ ] Testing (AC: #1, #2, #3)
  - [ ] Test approval email delivery
  - [ ] Test rejection email delivery
  - [ ] Test email failure handling
  - [ ] Test async email sending

---

## Dev Notes

### Previous Story Intelligence

**From Story 2.4 (Admin Approve/Reject Listings):**
- AdminController has approveListing() and rejectListing() methods
- GameAccountService.updateStatus() handles status changes
- Listing status changes from PENDING → APPROVED or REJECTED

**Database Schema:**
- `game_accounts` table has: seller_id, status, rejection_reason, game_name, account_rank, price
- `users` table has: email (for recipient address)
- Need to JOIN with users table to get seller email

### Email Templates

**Approval Email Template:**

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
        .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
        .listing-info { background: white; padding: 15px; margin: 15px 0; border-radius: 4px; }
        .button { display: inline-block; padding: 12px 30px; background: #27ae60; color: white; text-decoration: none; border-radius: 4px; }
        .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>✅ Listing của bạn đã được duyệt!</h1>
        </div>
        <div class="content">
            <p>Chúc mừng! Listing của bạn đã được phê duyệt và hiện đã hiển thị trên trang chủ.</p>

            <div class="listing-info">
                <h3>Thông tin listing:</h3>
                <p><strong>Game:</strong> {gameName}</p>
                <p><strong>Rank:</strong> {accountRank}</p>
                <p><strong>Giá:</strong> {price} VNĐ</p>
            </div>

            <p>Listing của bạn giờ đã có thể được nhìn thấy bởi tất cả người mua. Chúc bạn bán được sớm!</p>

            <div style="text-align: center; margin: 20px 0;">
                <a href="{listingUrl}" class="button">Xem listing của bạn</a>
            </div>
        </div>
        <div class="footer">
            <p>Email này được gửi tự động từ Game Account Shop.</p>
        </div>
    </div>
</body>
</html>
```

**Rejection Email Template:**

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
        .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
        .listing-info { background: white; padding: 15px; margin: 15px 0; border-radius: 4px; }
        .reason { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0; }
        .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>❌ Listing của bạn đã bị từ chối</h1>
        </div>
        <div class="content">
            <p>Rất tiếc, listing của bạn đã bị từ chối sau khi xem xét.</p>

            <div class="listing-info">
                <h3>Thông tin listing:</h3>
                <p><strong>Game:</strong> {gameName}</p>
                <p><strong>Rank:</strong> {accountRank}</p>
                <p><strong>Giá:</strong> {price} VNĐ</p>
            </div>

            <div class="reason">
                <h3>Lý do từ chối:</h3>
                <p>{rejectionReason}</p>
            </div>

            <p>Vui lòng xem lại và sửa lại theo yêu cầu, sau đó đăng lại listing mới.</p>

            <p>Nếu bạn có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ với chúng tôi.</p>
        </div>
        <div class="footer">
            <p>Email này được gửi tự động từ Game Account Shop.</p>
        </div>
    </div>
</body>
</html>
```

### Service Layer: EmailService

**Create `EmailService.java`:**

```java
package com.gameaccountshop.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send approval email to seller
     * @param toEmail Seller's email address
     * @param gameName Game name
     * @param accountRank Account rank
     * @param price Listing price
     * @param listingUrl URL to view the listing
     */
    @Async
    public void sendListingApprovedEmail(String toEmail, String gameName,
                                         String accountRank, Long price, String listingUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("✅ Listing của bạn đã được duyệt!");

            String htmlContent = buildApprovalEmail(gameName, accountRank, price, listingUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Approval email sent to: {} for listing: {}", toEmail, gameName);

        } catch (MessagingException e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send approval email", e);
        }
    }

    /**
     * Send rejection email to seller
     * @param toEmail Seller's email address
     * @param gameName Game name
     * @param accountRank Account rank
     * @param price Listing price
     * @param rejectionReason Reason for rejection
     */
    @Async
    public void sendListingRejectedEmail(String toEmail, String gameName,
                                         String accountRank, Long price, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("❌ Listing của bạn đã bị từ chối");

            String htmlContent = buildRejectionEmail(gameName, accountRank, price, rejectionReason);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Rejection email sent to: {} for listing: {}", toEmail, gameName);

        } catch (MessagingException e) {
            log.error("Failed to send rejection email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }

    private String buildApprovalEmail(String gameName, String accountRank, Long price, String listingUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
                    .listing-info { background: white; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .button { display: inline-block; padding: 12px 30px; background: #27ae60; color: white; text-decoration: none; border-radius: 4px; }
                    .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Listing của bạn đã được duyệt!</h1>
                    </div>
                    <div class="content">
                        <p>Chúc mừng! Listing của bạn đã được phê duyệt và hiện đã hiển thị trên trang chủ.</p>

                        <div class="listing-info">
                            <h3>Thông tin listing:</h3>
                            <p><strong>Game:</strong> %s</p>
                            <p><strong>Rank:</strong> %s</p>
                            <p><strong>Giá:</strong> %s VNĐ</p>
                        </div>

                        <p>Listing của bạn giờ đã có thể được nhìn thấy bởi tất cả người mua. Chúc bạn bán được sớm!</p>

                        <div style="text-align: center; margin: 20px 0;">
                            <a href="%s" class="button">Xem listing của bạn</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Email này được gửi tự động từ Game Account Shop.</p>
                    </div>
                </div>
            </body>
            </html>
            """, gameName, accountRank, String.format("%,d", price), listingUrl);
    }

    private String buildRejectionEmail(String gameName, String accountRank, Long price, String rejectionReason) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
                    .listing-info { background: white; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .reason { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Listing của bạn đã bị từ chối</h1>
                    </div>
                    <div class="content">
                        <p>Rất tiếc, listing của bạn đã bị từ chối sau khi xem xét.</p>

                        <div class="listing-info">
                            <h3>Thông tin listing:</h3>
                            <p><strong>Game:</strong> %s</p>
                            <p><strong>Rank:</strong> %s</p>
                            <p><strong>Giá:</strong> %s VNĐ</p>
                        </div>

                        <div class="reason">
                            <h3>Lý do từ chối:</h3>
                            <p>%s</p>
                        </div>

                        <p>Vui lòng xem lại và sửa lại theo yêu cầu, sau đó đăng lại listing mới.</p>

                        <p>Nếu bạn có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ với chúng tôi.</p>
                    </div>
                    <div class="footer">
                        <p>Email này được gửi tự động từ Game Account Shop.</p>
                    </div>
                </div>
            </body>
            </html>
            """, gameName, accountRank, String.format("%,d", price), rejectionReason);
    }
}
```

### Updated GameAccountService

**Add email sending to status update methods:**

```java
@Service
@Slf4j
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ... existing code ...

    /**
     * Approve a pending listing and send email to seller
     */
    @Transactional
    public void approveListing(Long listingId) {
        log.info("Approving listing: {}", listingId);

        GameAccount listing = gameAccountRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        listing.setStatus(ListingStatus.APPROVED);
        gameAccountRepository.save(listing);

        // Send approval email to seller
        try {
            User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            String listingUrl = "https://yourdomain.com/listings/" + listingId;
            emailService.sendListingApprovedEmail(
                seller.getEmail(),
                listing.getGameName(),
                listing.getAccountRank(),
                listing.getPrice(),
                listingUrl
            );
            log.info("Approval email queued for: {}", seller.getEmail());
        } catch (Exception e) {
            log.error("Failed to send approval email for listing: {}", listingId, e);
            // Don't throw - listing status is already updated
        }
    }

    /**
     * Reject a pending listing and send email to seller
     */
    @Transactional
    public void rejectListing(Long listingId, String reason) {
        log.info("Rejecting listing: {} with reason: {}", listingId, reason);

        GameAccount listing = gameAccountRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        listing.setStatus(ListingStatus.REJECTED);
        listing.setRejectionReason(reason);
        gameAccountRepository.save(listing);

        // Send rejection email to seller
        try {
            User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            emailService.sendListingRejectedEmail(
                seller.getEmail(),
                listing.getGameName(),
                listing.getAccountRank(),
                listing.getPrice(),
                reason
            );
            log.info("Rejection email queued for: {}", seller.getEmail());
        } catch (Exception e) {
            log.error("Failed to send rejection email for listing: {}", listingId, e);
            // Don't throw - listing status is already updated
        }
    }
}
```

### Configuration: application.yml

**Add Gmail SMTP configuration:**

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: xxxx-xxxx-xxxx-xxxx
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Dependency: pom.xml

**Add Spring Boot Mail dependency:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### Enable Async Support

**Create or update `AsyncConfiguration.java`:**

```java
package com.gameaccountshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Enables @Async support for email sending
}
```

### Testing

**Test email service with mock:**

```java
@Test
void testSendApprovalEmail() {
    // Use MailServer mock for testing
    String testEmail = "test@example.com";
    emailService.sendListingApprovedEmail(
        testEmail,
        "Liên Minh Huyền Thoại",
        "Gold III",
        500000L,
        "http://localhost:8080/listings/1"
    );
    // Verify email was sent
}

@Test
void testSendRejectionEmail() {
    String testEmail = "test@example.com";
    emailService.sendListingRejectedEmail(
        testEmail,
        "Liên Minh Huyền Thoại",
        "Gold III",
        500000L,
        "Thông tin không chính xác"
    );
    // Verify email was sent
}
```

### Testing Checklist

- [ ] Approval email sends correctly when listing is approved
- [ ] Rejection email sends correctly when listing is rejected
- [ ] Email content is properly formatted (HTML)
- [ ] Emails contain correct listing information
- [ ] Email failures don't block status updates
- [ ] Async email sending works correctly
- [ ] Admin sees error messages if email fails
- [ ] Vietnamese characters display correctly
- [ ] Links in emails work correctly

### Error Handling

| Scenario | Expected Behavior |
|----------|-------------------|
| Email sends successfully | Log success, no error to admin |
| Email fails to send | Log error, show warning to admin, status still updates |
| Seller email not found | Log error, continue with status update |
| SMTP connection fails | Log error, show warning to admin |
| Invalid email format | Log error, continue with status update |

### Gmail Setup Instructions

For the developer/admin to set up Gmail App Password:

1. Go to Google Account → Security
2. Enable "2-Step Verification"
3. Go to "App Passwords" → Create new
4. Select "Mail" and your device
5. Copy the 16-character password (format: xxxx xxxx xxxx xxxx)
6. Paste into application.yml as `spring.mail.password`

### Security Considerations

- Never commit real passwords to version control
- Use environment variables for sensitive data
- App Password should be stored securely
- Consider using a transactional email service for production
- Rate limiting to prevent spam
- Validate email addresses before sending

### References

- [Source: planning-artifacts/epics.md#Story 2.7: Listing Email Notifications]
- [Spring Boot Mail Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/mail.html)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
- [Story 2.4: Admin Approve/Reject Listings]

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Completion Notes List

- **Story Status:** New story - not yet implemented
- **Dependencies:** Story 2.4 (Admin Approve/Reject Listings) must be completed first
- **Configuration Required:** Gmail App Password in application.yml
- **External Service:** Gmail SMTP

### File List

**Files to Create:**
- `src/main/java/com/gameaccountshop/service/EmailService.java`
- `src/main/java/com/gameaccountshop/config/AsyncConfiguration.java`
- `src/test/java/com/gameaccountshop/service/EmailServiceTest.java`

**Files to Modify:**
- `src/main/java/com/gameaccountshop/service/GameAccountService.java` - Add email sending to approve/reject methods
- `src/main/java/com/gameaccountshop/controller/AdminController.java` - Update to handle email errors
- `src/main/resources/application.yml` - Add Gmail SMTP configuration
- `pom.xml` - Add spring-boot-starter-mail dependency
