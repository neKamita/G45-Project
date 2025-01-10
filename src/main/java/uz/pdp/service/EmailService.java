package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private static JavaMailSender mailSender;

    public void sendSellerApprovalEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Congratulations! You are now a Seller");
        message.setText("Dear user,\n\nYou have been approved as a seller. Welcome aboard!\n\nBest regards,\nYour Team");

        mailSender.send(message);
    }
}
