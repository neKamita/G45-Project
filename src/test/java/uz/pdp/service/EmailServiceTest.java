package uz.pdp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import uz.pdp.payload.EntityResponse;

import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;
    
    @InjectMocks
    private EmailService emailService;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @BeforeEach
    void setUp() {
        // Enable email validation for tests
        ReflectionTestUtils.setField(emailService, "emailValidationEnabled", true);
    }

    @Test
    void sendVerificationEmail_Success() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        EntityResponse<String> response = emailService.sendVerificationEmail(email);

        // Assert
        assertTrue(response.success());
        assertEquals("Email sent successfully", response.message());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmail_InvalidEmail() {
        // Arrange
        String invalidEmail = "not-an-email";
        
        // Act
        EntityResponse<String> response = emailService.sendVerificationEmail(invalidEmail);
        
        // Assert
        assertFalse(response.success());
        assertEquals("Invalid email format", response.message());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmail_MailServerError() {
        // Arrange
        String email = "test@example.com";
        doThrow(new MailSendException("Failed to send"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        EntityResponse<String> response = emailService.sendVerificationEmail(email);

        // Assert
        assertFalse(response.success());
        assertEquals("Failed to send email", response.message());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}