package dev.sara.micos_color_code.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.frontend.url=http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendConfirmationEmail(String to, String username, String confirmationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            
            String confirmationLink = "http://localhost:8080/register/confirm?token=" + confirmationToken;
            
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("confirmationLink", confirmationLink);

            String htmlContent = templateEngine.process("confirmation-email", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Confirma tu cuenta - Micos Color Code");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de confirmación enviado a: {}", to);

        } catch (MessagingException e) {
            log.error("Error al enviar email de confirmación a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email de confirmación", e);
        }
    }
}