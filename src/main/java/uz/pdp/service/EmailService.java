package uz.pdp.service;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uz.pdp.payload.EntityResponse;

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

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
