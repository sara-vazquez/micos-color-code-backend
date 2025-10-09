package dev.sara.micos_color_code.register;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponseDTO {
    private String username;
    private String role;
}
