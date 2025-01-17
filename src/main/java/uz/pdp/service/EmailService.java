package uz.pdp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uz.pdp.payload.EntityResponse;

import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;

/**
 * Service class for handling all email-related operations.
 * Provides functionality for sending various types of emails including
 * verification codes, notifications, and HTML-formatted messages.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
@Slf4j
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Validates an email address format.
     *
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Sends a simple text email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param text Email content
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendSimpleEmail(String to, String subject, String text) {
        try {
            if (!isValidEmail(to)) {
                logger.error("Invalid email address: {}", to);
                return new EntityResponse<>("Invalid email address", false, null);
            }

            logger.info("Sending simple email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);
            return new EntityResponse<>("Email sent successfully", true, null);
        } catch (MailSendException e) {
            logger.error("Failed to send simple email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends an HTML-formatted email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML-formatted content
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            if (!isValidEmail(to)) {
                logger.error("Invalid email address: {}", to);
                return new EntityResponse<>("Invalid email address", false, null);
            }

            logger.info("Sending HTML email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
            return new EntityResponse<>("Email sent successfully", true, null);
        } catch (MessagingException | MailSendException e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends a verification code email for seller registration.
     *
     * @param to Recipient email address
     * @param verificationCode Verification code
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendSellerVerificationEmail(String to, String verificationCode) {
        try {
            logger.info("Sending seller verification email to: {}", to);
            String subject = "Seller Account Verification";
            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Seller Account Verification</h2>
                    <p>Your verification code is: <strong>%s</strong></p>
                    <p>This code will expire in 15 minutes.</p>
                    <p>If you did not request this verification, please ignore this email.</p>
                </body>
                </html>
                """, verificationCode);

            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send seller verification email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send verification email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends an order confirmation email.
     *
     * @param to Recipient email address
     * @param orderNumber Order number
     * @param orderDetails Order details in HTML format
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendOrderConfirmationEmail(String to, String orderNumber, String orderDetails) {
        try {
            logger.info("Sending order confirmation email to: {}", to);
            String subject = "Order Confirmation - " + orderNumber;
            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Order Confirmation</h2>
                    <p>Thank you for your order!</p>
                    <p>Order Number: <strong>%s</strong></p>
                    %s
                    <p>We will notify you when your order ships.</p>
                </body>
                </html>
                """, orderNumber, orderDetails);

            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send order confirmation: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends a password reset email.
     *
     * @param to Recipient email address
     * @param resetToken Password reset token
     * @param resetLink Password reset link
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendPasswordResetEmail(String to, String resetToken, String resetLink) {
        try {
            logger.info("Sending password reset email to: {}", to);
            String subject = "Password Reset Request";
            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>You have requested to reset your password.</p>
                    <p>Click the link below to reset your password:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>This link will expire in 30 minutes.</p>
                    <p>If you did not request this reset, please ignore this email.</p>
                </body>
                </html>
                """, resetLink);

            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send password reset email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Generates a random verification code.
     *
     * @return Generated verification code
     */
    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /**
     * Generates a unique token for password reset.
     *
     * @return Generated token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
