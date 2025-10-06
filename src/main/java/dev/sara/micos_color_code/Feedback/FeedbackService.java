package dev.sara.micos_color_code.Feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendFeedback(FeedbackRequestDTO feedback) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("micoscolorcode@gmail.com");
        message.setSubject("Nuevo feedback de Micos");
        message.setText("📧 De: " + feedback.getEmail() + "\n\n📝 Mensaje:\n" + feedback.getMessage());
        mailSender.send(message);
    }
}
