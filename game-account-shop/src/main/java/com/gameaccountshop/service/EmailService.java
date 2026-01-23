package com.gameaccountshop.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
            // We log but don't rethrow to avoid affecting the calling transaction if any
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
        }
    }

    /**
     * Send account credentials email to buyer after purchase
     * @param toEmail Buyer's email address
     * @param gameName Game name
     * @param accountRank Account rank
     * @param username Account username
     * @param password Account password
     * @param notes Additional notes
     */
    @Async
    public void sendAccountCredentialsEmail(String toEmail, String gameName,
                                            String accountRank, String username,
                                            String password, String notes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎮 Thông tin tài khoản game - " + gameName);

            String htmlContent = buildCredentialsEmail(gameName, accountRank, username, password, notes);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Account credentials email sent to: {} for game: {}", toEmail, gameName);

        } catch (MessagingException e) {
            log.error("Failed to send credentials email to: {}", toEmail, e);
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

    private String buildCredentialsEmail(String gameName, String accountRank,
                                         String username, String password, String notes) {
        String notesSection = (notes != null && !notes.trim().isEmpty())
            ? String.format("""
                <div class="notes-section">
                    <h3>📝 Ghi chú thêm:</h3>
                    <p>%s</p>
                </div>
                """, notes.replace("\n", "<br>"))
            : "";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 25px; text-align: center; border-radius: 12px 12px 0 0; }
                    .content { background: #f8f9fa; padding: 25px; border-radius: 0 0 12px 12px; }
                    .account-info { background: white; padding: 20px; margin: 20px 0; border-radius: 8px; border: 2px solid #667eea; }
                    .credential-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #eee; }
                    .credential-row:last-child { border-bottom: none; }
                    .credential-label { font-weight: bold; color: #555; }
                    .credential-value { color: #2c3e50; font-family: monospace; font-size: 16px; }
                    .notes-section { background: #fff3cd; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107; margin: 20px 0; }
                    .warning { background: #f8d7da; padding: 15px; border-radius: 8px; border-left: 4px solid #dc3545; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎮 Thông tin tài khoản game</h1>
                    </div>
                    <div class="content">
                        <p>Chúc mừng! Bạn đã mua thành công tài khoản game.</p>

                        <div class="account-info">
                            <h3 style="margin-top: 0;">📋 Thông tin tài khoản:</h3>
                            <div class="credential-row">
                                <span class="credential-label">Game:</span>
                                <span class="credential-value">%%s</span>
                            </div>
                            <div class="credential-row">
                                <span class="credential-label">Rank:</span>
                                <span class="credential-value">%%s</span>
                            </div>
                            <div class="credential-row">
                                <span class="credential-label">Username:</span>
                                <span class="credential-value">%%s</span>
                            </div>
                            <div class="credential-row">
                                <span class="credential-label">Password:</span>
                                <span class="credential-value">%%s</span>
                            </div>
                        </div>

                        %%s

                        <div class="warning">
                            <strong>⚠️ Lưu ý quan trọng:</strong>
                            <ul style="margin: 10px 0 0 20px; padding: 0;">
                                <li>Vui lòng đổi mật khẩu ngay sau khi đăng nhập</li>
                                <li>Không chia sẻ thông tin tài khoản cho người khác</li>
                                <li>Lưu thông tin này ở nơi an toàn</li>
                            </ul>
                        </div>

                        <p>Cảm ơn bạn đã mua hàng tại Game Account Shop!</p>
                    </div>
                    <div class="footer">
                        <p>Email này được gửi tự động từ Game Account Shop.</p>
                    </div>
                </div>
            </body>
            </html>
            """, gameName, accountRank, username, password, notesSection);
    }
}
