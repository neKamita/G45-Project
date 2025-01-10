package uz.pdp.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }



    public void sendVerificationEmail(String toEmail) {
        String verificationCode = UUID.randomUUID().toString().substring(0, 6);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Seller Verification Code");
            message.setText("Your verification code is: " + verificationCode + 
                "\nPlease enter this code to verify your seller request.");

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
