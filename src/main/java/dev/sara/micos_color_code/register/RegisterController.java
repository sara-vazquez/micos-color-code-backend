package dev.sara.micos_color_code.register;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.sara.micos_color_code.Captcha.CaptchaService;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;
    private final CaptchaService captchaService;

    public RegisterController(RegisterService registerService, CaptchaService captchaService) {
        this.registerService = registerService;
        this.captchaService = captchaService;
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
    public String confirmAccount(@RequestParam("token") String token) {
        try {
            registerService.confirmAccount(token);
            return "redirect:http://localhost:5173/login?confirmed=true";
        } catch (RuntimeException e) {
            return "redirect:http://localhost:5173/login?error=" + e.getMessage();
        }
    }
}
