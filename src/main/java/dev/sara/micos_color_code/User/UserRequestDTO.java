package dev.sara.micos_color_code.user;

public record UserRequestDTO( String username, String email, String password, String captchaId, int captchaAnswer) {}
