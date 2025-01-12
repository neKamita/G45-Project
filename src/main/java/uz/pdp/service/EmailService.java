package uz.pdp.service;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uz.pdp.payload.EntityResponse;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {
    


    private final JavaMailSender mailSender;
    private final String fromEmail;

    @Value("${email.validation.enabled:true}")
    private boolean emailValidationEnabled;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public EntityResponse<String> sendVerificationEmail(String toEmail) {
        // First validate email format
        if (!isValidEmail(toEmail)) {
            log.warn("Invalid email format: {}", toEmail);
            return EntityResponse.error("Invalid email format");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification");
            message.setText("Your verification code is: " + generateVerificationCode());

            if (emailValidationEnabled) {
                mailSender.send(message);
                log.info("Verification email sent successfully to: {}", toEmail);
                return EntityResponse.success("Email sent successfully");
            } else {
                log.info("Email validation disabled. Would have sent to: {}", toEmail);
                return EntityResponse.success("Email validation disabled");
            }
        } catch (MailSendException e) {
            log.error("Failed to send verification email to: {}", toEmail);
            return EntityResponse.error("Failed to send email");
        }
    }

    public EntityResponse<String> sendVerificationEmail(String toEmail, String verificationCode) {
        if (!isValidEmail(toEmail)) {
            log.warn("Invalid email format: {}", toEmail);
            return EntityResponse.error("Invalid email format");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🚀 Etadoor Seller Verification");
            
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                        <h1 style="color: #333; margin-bottom: 20px;">ETADOOR VERIFICATION</h1>
                        
                        <p style="font-size: 18px; color: #666;">Hello Future Seller! 🎉</p>
                        
                        <div style="margin: 30px 0;">
                            <p style="font-size: 16px; color: #666;">Your verification code is:</p>
                            
                            <div style="background-color: #fff; border: 2px solid #ddd; border-radius: 10px; 
                                      padding: 20px; margin: 20px 0; font-size: 32px; letter-spacing: 5px;">
                                %s
                            </div>
                        </div>
                        
                        <p style="color: #dc3545; font-size: 14px;">⚠️ This code will expire in 15 minutes</p>
                        
                        <div style="background-color: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="color: #333; margin-bottom: 10px;">🔒 Security Notice</h3>
                            <ul style="text-align: left; color: #666;">
                                <li>Keep this code private</li>
                                <li>Never share it with anyone</li>
                                <li>Our team will never ask for it</li>
                            </ul>
                        </div>
                        
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
                            <p style="color: #666;">Thank you for joining Etadoor!</p>
                            <p style="color: #666;">Best regards,<br>The Etadoor Team 🏪</p>
                        </div>
                    </div>
                </div>
                """, verificationCode);

            helper.setText(htmlContent, true);

            if (emailValidationEnabled) {
                mailSender.send(mimeMessage);
                log.info("Verification email sent successfully to: {}", toEmail);
                return EntityResponse.success("Email sent successfully");
            } else {
                log.info("Email validation disabled. Code: {} for: {}", verificationCode, toEmail);
                return EntityResponse.success("Email validation disabled");
            }
        } catch (MessagingException | MailSendException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            return EntityResponse.error("Failed to send email");
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
