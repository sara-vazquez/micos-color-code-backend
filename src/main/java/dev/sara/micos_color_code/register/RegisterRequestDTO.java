package dev.sara.micos_color_code.register;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {
    private String username;
    private String email;
    private String password;
    private String captchaId; 
    private int captchaAnswer;
}
