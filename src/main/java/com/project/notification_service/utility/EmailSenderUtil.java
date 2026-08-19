package com.project.notification_service.utility;

import com.project.notification_service.exception.EmailSendingException;
import com.project.notification_service.security.SecurityUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderUtil {

    private final JavaMailSender mailSender;
    private final SecurityUtils securityUtils;

    @Value("${spring.mail.username}")
    private String defaultSender;

    public EmailSenderUtil(JavaMailSender mailSender, SecurityUtils securityUtils) {
        this.mailSender = mailSender;
        this.securityUtils = securityUtils;
    }

    public void sendEmail(String subject, String htmlContent, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(defaultSender);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            //mailSender.send(message);
            throw new MailException("Email sent successfully to " + recipientEmail) {};
        } catch (MailException | MessagingException e) {
            throw new EmailSendingException("Failed to send email to " + recipientEmail + ": " + e.getMessage());
        }
    }
}
