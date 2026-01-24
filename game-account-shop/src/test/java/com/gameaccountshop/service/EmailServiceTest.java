package com.gameaccountshop.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void sendListingApprovedEmail_ShouldSendEmail() {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendListingApprovedEmail(
                "seller@example.com",
                "Game Name",
                "Gold",
                100000L,
                "http://localhost:8080/listings/1"
        );

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendListingRejectedEmail_ShouldSendEmail() {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendListingRejectedEmail(
                "seller@example.com",
                "Game Name",
                "Gold",
                100000L,
                "Reason"
        );

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // Story 3.2: Top-up Email Tests

    @Test
    void sendTopUpApprovedEmail_ShouldSendEmail() {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendTopUpApprovedEmail(
                "user@example.com",
                new BigDecimal("500000"),
                new BigDecimal("1500000"),
                "TXN123"
        );

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendTopUpApprovedEmail_WhenMessagingException_ShouldNotThrow() {
        // Arrange
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        // Act & Assert - should not throw exception, just log error
        Assertions.assertDoesNotThrow(() -> emailService.sendTopUpApprovedEmail(
                "user@example.com",
                new BigDecimal("500000"),
                new BigDecimal("1500000"),
                "TXN123"
        ));
    }

    @Test
    void sendTopUpRejectedEmail_ShouldSendEmail() {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendTopUpRejectedEmail(
                "user@example.com",
                new BigDecimal("500000"),
                "Giao dịch không hợp lệ",
                "TXN123"
        );

        // Assert
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendTopUpRejectedEmail_WhenMessagingException_ShouldNotThrow() {
        // Arrange
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        // Act & Assert - should not throw exception, just log error
        Assertions.assertDoesNotThrow(() -> emailService.sendTopUpRejectedEmail(
                "user@example.com",
                new BigDecimal("500000"),
                "Giao dịch không hợp lệ",
                "TXN123"
        ));
    }
}
