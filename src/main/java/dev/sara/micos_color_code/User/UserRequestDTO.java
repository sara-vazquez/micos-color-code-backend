package dev.sara.micos_color_code.User;

public record UserRequestDTO( String username, String email, String password, String captchaId, int captchaAnswer) {}
