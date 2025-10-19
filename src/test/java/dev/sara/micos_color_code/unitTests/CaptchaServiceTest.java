package dev.sara.micos_color_code.unitTests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sara.micos_color_code.captcha.CaptchaService;

@ExtendWith(MockitoExtension.class)
public class CaptchaServiceTest {

    private CaptchaService captchaService;
    
    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService();
    }

    @Test
    void generate_ShouldReturnChallenge_WithValidIdAndQuestion() {
        var challenge = captchaService.generate();

        assertThat(challenge).isNotNull();
        assertThat(challenge.id())
                .isNotNull()
                .matches("^[0-9a-f\\-]{36}$");
        assertThat(challenge.question())
                .isNotBlank()
                .contains("¿Cuánto es");
    }

    @Test
    void validate_ShouldReturnFalse_WhenAnswerIsIncorrect() {
        var challenge = captchaService.generate();
        boolean result = captchaService.validate(challenge.id(), 99999);

        assertThat(result).isFalse();
    }
    
}
