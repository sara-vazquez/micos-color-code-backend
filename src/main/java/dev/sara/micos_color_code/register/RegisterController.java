package dev.sara.micos_color_code.register;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import dev.sara.micos_color_code.captcha.CaptchaService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import dev.sara.micos_color_code.util.EmailService;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;
    private final CaptchaService captchaService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public RegisterController(RegisterService registerService, CaptchaService captchaService, UserRepository userRepository, EmailService emailService) {
        this.registerService = registerService;
        this.captchaService = captchaService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        
        if (!captchaService.validate(request.getCaptchaId(), request.getCaptchaAnswer())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "Captcha inválido"));
        }

        RegisterResponseDTO response = registerService.register(request);
    return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    public RedirectView confirmAccount(@RequestParam("token") String token) {
        try {
            registerService.confirmAccount(token);
            return new RedirectView("http://localhost:5173/login?confirmed=true");
        } catch (RuntimeException e) {
            return new RedirectView("http://localhost:5173/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmation(@RequestParam String email) {
        try {
            UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
            if (user.isEnabled()) {
                return ResponseEntity.ok(Map.of("message", "La cuenta ya está verificada"));
            }
        
            // Generate new token
            String newToken = UUID.randomUUID().toString();
            user.setConfirmationToken(newToken);
            user.setTokenCreationDate(LocalDateTime.now());
            userRepository.save(user);
        
            // Resend email
            emailService.sendConfirmationEmail(user.getEmail(), user.getUsername(), newToken);
        
            return ResponseEntity.ok(Map.of("message", "Email de confirmación reenviado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
