package dev.sara.micos_color_code.Feedback;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    
    private final JavaMailSender mailSender;

}
