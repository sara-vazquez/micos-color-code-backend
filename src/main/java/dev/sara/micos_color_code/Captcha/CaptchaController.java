package dev.sara.micos_color_code.Captcha;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/generate")
    public ResponseEntity<?> generateCaptcha() {
        CaptchaService.CaptchaChallenge captcha = captchaService.generate();
        return ResponseEntity.ok(captcha);
    }
}
