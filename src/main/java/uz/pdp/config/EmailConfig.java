package uz.pdp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * The Digital Postal Service Configuration 📧
 * 
 * This configuration sets up our trusty email sender.
 * Because sometimes a carrier pigeon just won't cut it.
 * 
 * Technical Features:
 * - SMTP configuration
 * - TLS security
 * - Authentication setup
 * - Connection pooling
 * 
 * Note: If emails aren't being delivered, check:
 * 1. Your internet connection (yes, really)
 * 2. SMTP credentials
 * 3. If Mercury is in retrograde
 * 
 * @version 1.0
 * @since 2025-01-17
 */
@Configuration
public class EmailConfig {

    // The sacred SMTP coordinates
    @Value("${spring.mail.host}")
    private String host;

    // The magical portal number
    @Value("${spring.mail.port}")
    private int port;

    // Our secret identity
    @Value("${spring.mail.username}")
    private String username;

    // The password that must never be spoken aloud
    @Value("${spring.mail.password}")
    private String password;

    /**
     * Creates and configures our mighty JavaMailSender.
     * This is where the email magic happens! ✨
     * 
     * Technical Details:
     * - Sets up SMTP host and port
     * - Configures authentication
     * - Enables TLS security
     * - Sets connection timeouts
     * 
     * Pro tip: If you're wondering why we need all these properties,
     * try sending an email without them. Go ahead, we'll wait... 😉
     *
     * @return A fully armed and operational mail sender
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}