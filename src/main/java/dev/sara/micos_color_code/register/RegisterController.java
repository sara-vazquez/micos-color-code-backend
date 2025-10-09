package dev.sara.micos_color_code.register;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import dev.sara.micos_color_code.auth.AuthResponseDTO;

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
}
