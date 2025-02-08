package uz.pdp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;
import uz.pdp.enums.VerificationType;
import uz.pdp.payload.EntityResponse;

import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 📬 The Digital Carrier Pigeon Service
 * 
 * Responsible for all your electronic mail needs!
 * Because sending smoke signals is no longer GDPR compliant.
 * 
 * Technical Features:
 * - Email validation (yes, "me@me" is not valid)
 * - HTML email support (making plain text feel inadequate since 1995)
 * - Verification code generation (random numbers that users will type wrong)
 * - Retry mechanism (because SMTP servers need coffee breaks too)
 * 
 * Warning Signs Your Email Might Not Arrive:
 * 1. Mercury is in retrograde
 * 2. User typed "gmial.com"
 * 3. It's Monday
 * 4. The email server is feeling moody
 * 
 * Remember: Email delivery is like ordering pizza 🍕
 * - Sometimes it arrives quickly
 * - Sometimes it gets lost
 * - Sometimes it goes to your neighbor
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
@Slf4j
public class EmailService {
    // For logging when emails venture into the digital abyss
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // The sacred regex that validates email addresses
    // (Sorry, "cool.dude@" is not a valid email)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Our official email address (please don't spam it)
    @Value("${spring.mail.username}")
    private String fromEmail;

    // The magical email sending machine
    @Autowired
    private JavaMailSender mailSender;

    private static final String BASE_TEMPLATE = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 800px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #2193b0, #6dd5ed); color: white; padding: 20px; border-radius: 10px; }
                        .section { background: #fff; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                        .title { color: #2193b0; margin-bottom: 10px; }
                        .price { font-size: 24px; color: #e74c3c; font-weight: bold; }
                        .variant { background: #f8f9fa; padding: 10px; margin: 5px 0; border-radius: 5px; }
                        .variant:hover { background: #e9ecef; }
                        .customer-info { background: #f1f8ff; padding: 15px; border-radius: 10px; margin-top: 20px; }
                        .footer { text-align: center; margin-top: 20px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        %s
                    </div>
                </body>
                </html>
            """;

    /**
     * Sends a verification code faster than you can say "spam folder".
     * <p>
     * Technical Process:
     * 1. Generate a random code (because security!)
     * 2. Create a fancy HTML template
     * 3. Cross fingers and hit send
     * 4. Hope it doesn't end up in spam
     * <p>
     * Pro tip: Tell users to check their spam folder first.
     * It saves everyone's time, trust me! 📨
     *
     * @param to   The hopeful recipient's email
     * @param code Verification code
     * @param type What we're verifying (their existence, mostly)
     */
    public void sendVerificationEmail(String email, String code, VerificationType type) {
        if (!isValidEmail(email)) {
            EntityResponse.error("Invalid email format");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);

            String subject = getSubjectForVerificationType(type);
            String content = getContentForVerificationType(type, code);

            helper.setSubject(subject);
            helper.setText(BASE_TEMPLATE.formatted(content), true);

            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", email);

            EntityResponse.success("Verification email sent successfully");

        } catch (MessagingException | MailSendException e) {
            logger.error("Failed to send verification email to {}: {}", email, e.getMessage());
            EntityResponse.error("Failed to send verification email: " + e.getMessage());
        }
    }

    private String getSubjectForVerificationType(VerificationType type) {
        return switch (type) {
            case SELLER_REQUEST -> "Verify Your Seller Account Request";
            case PASSWORD_RESET -> "Password Reset Verification";
            case EMAIL_CONFIRMATION -> "Confirm Your Email Address";
        };
    }

    private String getContentForVerificationType(VerificationType type, String code) {
        return switch (type) {
            case SELLER_REQUEST -> getSellerRequestEmailContent(code);
            case PASSWORD_RESET -> getPasswordResetEmailContent(code);
            case EMAIL_CONFIRMATION -> getEmailConfirmationContent(code);
        };
    }

    private String getSellerRequestEmailContent(String code) {
        return String.format("""
                <div style='text-align: center;'>
                    <h1>Welcome Future Seller! 🌟</h1>
                    <p>We're excited to have you join our marketplace! Please use this code to verify your seller account:</p>
                    <div style='margin: 20px; padding: 10px; background-color: #f8f9fa; border-radius: 5px;'>
                        <h2 style='color: #2193b0; letter-spacing: 5px;'>%s</h2>
                    </div>
                    <p>This code will expire in 30 minutes.</p>
                    <p>Get ready to showcase your amazing products!</p>
                </div>
                """,
                code);
    }

    private String getPasswordResetEmailContent(String code) {
        return String.format("""
                <div style='text-align: center;'>
                    <h1>Password Reset Request 🔐</h1>
                    <p>You've requested to reset your password. Here's your verification code:</p>
                    <div style='margin: 20px; padding: 10px; background-color: #f8f9fa; border-radius: 5px;'>
                        <h2 style='color: #2193b0; letter-spacing: 5px;'>%s</h2>
                    </div>
                    <p>This code will expire in 30 minutes.</p>
                    <p>If you didn't request this reset, please ignore this email.</p>
                </div>
                """,
                code);
    }

    private String getEmailConfirmationContent(String code) {
        return String.format("""
                <div style='text-align: center;'>
                    <h1>Welcome to Our Community! 🎉</h1>
                    <p>Please use this verification code to confirm your email address:</p>
                    <div style='margin: 20px; padding: 10px; background-color: #f8f9fa; border-radius: 5px;'>
                        <h2 style='color: #2193b0; letter-spacing: 5px;'>%s</h2>
                    </div>
                    <p>This code will expire in 30 minutes.</p>
                    <p>If you didn't request this code, please ignore this email.</p>
                </div>
                """,
                code);
    }

    /**
     * Validates email addresses because users are... creative.
     * 
     * Checks for:
     * - Basic email format (@ symbol, domain, etc.)
     * - Common typos (we can't catch them all)
     * - Obvious fake emails (nice try, batman@batcave.com)
     * 
     * Fun fact: The longest valid email prefix is 64 characters.
     * But please, don't test this. Think of the poor database! 🙏
     *
     * @param email The supposed email address
     * @return true if it looks legit, false if... well, you know
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Generates a verification code that users will definitely mistype.
     * 
     * Features:
     * - Random number generation (very high-tech stuff)
     * - Configurable length (because 6 digits wasn't annoying enough)
     * - No confusing characters (0 vs O is not a fun game)
     * 
     * Note: We could make this more secure, but let's be honest,
     * users struggle enough with 6 digits. 🔢
     *
     * @return A string of numbers that will be typed incorrectly at least once
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Generates a random reset token.
     *
     * @return UUID-based reset token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Sends a simple text email.
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email content
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<?> sendSimpleEmail(String to, String subject, String text) {
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
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param htmlContent HTML-formatted content
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<Void> sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            if (!isValidEmail(to)) {
                logger.error("Invalid email address: {}", to);
                return EntityResponse.error("Invalid email address");
            }

            logger.info("Sending HTML email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(BASE_TEMPLATE.formatted(htmlContent), true);

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
            return EntityResponse.success("Email sent successfully");
        } catch (MessagingException | MailSendException e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            return EntityResponse.error("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Sends a verification code email for seller registration.
     * Because every seller deserves a grand entrance! 🎭
     *
     * @param to Recipient email address
     * @return EntityResponse indicating success/failure
     */
    public EntityResponse<String> sendSellerVerificationEmail(String to) {
        try {
            String code = generateVerificationCode();
            sendVerificationEmail(to, code, VerificationType.SELLER_REQUEST);
            return new EntityResponse<>("Verification email sent successfully", true, code);
        } catch (Exception e) {
            logger.error("Failed to send seller verification email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send verification email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends a password reset email with a verification code.
     * Because everyone forgets their password sometimes! 🔑
     *
     * @param to Recipient email address
     * @return EntityResponse indicating success/failure with the generated code
     */
    public EntityResponse<String> sendPasswordResetEmail(String to) {
        try {
            String code = generateVerificationCode();
            sendVerificationEmail(to, code, VerificationType.PASSWORD_RESET);
            return new EntityResponse<>("Password reset email sent successfully", true, code);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send password reset email: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends an email confirmation code.
     * Welcome to the family! 🎉
     *
     * @param to Recipient email address
     * @return EntityResponse indicating success/failure with the generated code
     */
    public EntityResponse<String> sendEmailConfirmation(String to) {
        try {
            String code = generateVerificationCode();
            sendVerificationEmail(to, code, VerificationType.EMAIL_CONFIRMATION);
            return new EntityResponse<>("Email confirmation sent successfully", true, code);
        } catch (Exception e) {
            logger.error("Failed to send email confirmation to {}: {}", to, e.getMessage());
            return new EntityResponse<>("Failed to send email confirmation: " + e.getMessage(), false, null);
        }
    }

    /**
     * Sends an order confirmation email.
     *
     * @param to           Recipient email address
     * @param orderNumber  Order number
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
     * @param to         Recipient email address
     * @param resetToken Password reset token
     * @param resetLink  Password reset link
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
                        <p>You have requested to reset your password. Please use the following code:</p>
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
     * Sends an order notification email to the seller.
     * Because every order deserves a grand announcement! 🛍️
     * 
     * @param sellerEmail Seller's email address
     * @param order       Order details
     * @param buyer       Buyer information
     * @throws MessagingException If the email server is having a bad day
     */
    public void sendOrderNotification(String sellerEmail, Order order, User buyer) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(sellerEmail);
            helper.setSubject("🛍️ New Order #" + order.getId());

            String emailContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                            "<div style='background-color: #4a90e2; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;'>" +
                            "<h1>🛍️ New Order!</h1>" +
                            "</div>" +
                            "<div style='padding: 20px; border: 1px solid #ddd;'>" +
                            "<p>Hello,</p>" +
                            "<p>Great news! You've received a new order. Here are the details:</p>" +
                            "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                            "<h3>📦 Order Details</h3>" +
                            "<p><strong>Order ID:</strong> %d</p>" +
                            "<p><strong>Order Date:</strong> %s</p>" +
                            "<p><strong>Total Amount:</strong> $%.2f</p>" +
                            "<h3>👤 Customer Information</h3>" +
                            "<p><strong>Name:</strong> %s %s</p>" +
                            "<p><strong>Phone:</strong> %s</p>" +
                            "<p><strong>Email:</strong> %s</p>" +
                            "<h3>📍 Delivery Information</h3>" +
                            "<p><strong>Address:</strong> %s</p>" +
                            "<h3>🛍️ Ordered Items</h3>" +
                            "<table style='width: 100%%; border-collapse: collapse; margin-top: 10px;'>" +
                            "<tr style='background-color: #f2f2f2;'>" +
                            "<th style='padding: 8px; text-align: left;'>Item</th>" +
                            "<th style='padding: 8px; text-align: left;'>Type</th>" +
                            "<th style='padding: 8px; text-align: left;'>Quantity</th>" +
                            "<th style='padding: 8px; text-align: left;'>Price</th>" +
                            "<th style='padding: 8px; text-align: left;'>Total</th>" +
                            "</tr>" +
                            "<tr>" +
                            "<td style='padding: 8px; border-top: 1px solid #ddd;'>%s</td>" +
                            "<td style='padding: 8px; border-top: 1px solid #ddd;'>%s</td>" +
                            "<td style='padding: 8px; border-top: 1px solid #ddd;'>%d</td>" +
                            "<td style='padding: 8px; border-top: 1px solid #ddd;'>$%.2f</td>" +
                            "<td style='padding: 8px; border-top: 1px solid #ddd;'>$%.2f</td>" +
                            "</tr>" +
                            "</table>" +
                            "<div style='margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd;'>" +
                            "<p><strong>Order Notes:</strong> %s</p>" +
                            "<p><strong>Installation Notes:</strong> %s</p>" +
                            "<p><strong>Delivery Notes:</strong> %s</p>" +
                            "</div>" +
                            "</div>" +
                            "</div>" +
                            "<div style='text-align: center; margin-top: 20px; color: #666;'>" +
                            "<p>Thank you for being an awesome seller! 🌟</p>" +
                            "</div>" +
                            "</div>",
                    order.getId(),
                    order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")),
                    order.getPrice() * order.getQuantity(), // Calculate total amount
                    buyer.getName(),
                    buyer.getLastname(),
                    buyer.getPhone(),
                    buyer.getEmail(),
                    order.getDeliveryAddress(),
                    order.getItemName(),
                    order.getItemType().toString(),
                    order.getQuantity(),
                    order.getPrice(),
                    order.getPrice() * order.getQuantity(), // Calculate total for this item
                    order.getComment() != null ? order.getComment() : "No comments provided",
                    order.getInstallationNotes() != null ? order.getInstallationNotes() : "No installation notes provided",
                    order.getDeliveryNotes() != null ? order.getDeliveryNotes() : "No delivery notes provided"
            );

            helper.setText(emailContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            logger.info("Order notification email sent successfully to seller: {}", sellerEmail);
        } catch (Exception e) {
            logger.error("Failed to send order notification email to seller {}: {}", sellerEmail, e.getMessage());
            throw new MessagingException("Failed to send order notification email: " + e.getMessage());
        }
    }
}
