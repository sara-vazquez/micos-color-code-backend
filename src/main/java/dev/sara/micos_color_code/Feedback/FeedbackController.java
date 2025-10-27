package dev.sara.micos_color_code.feedback;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/feedback")
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> sendFeedback(@RequestBody FeedbackRequestDTO feedback) {
    
        System.out.println("🔍 DTO deserializado:");
        System.out.println("📧 Email: " + feedback.getEmail());
        System.out.println("📝 Mensaje: " + feedback.getMessage());
    
        feedbackService.sendFeedback(feedback);
    
        return ResponseEntity.ok("Feedback enviado con éxito! ✅");
    }
}
