package dev.sara.micos_color_code.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import dev.sara.micos_color_code.register.RegisterRequestDTO;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping(path ="/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        if (!captchaService.validate(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new RuntimeException("Captcha inválido");
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        authService.logout(jwt);
        return ResponseEntity.ok("Logout exitoso");
    }

}
