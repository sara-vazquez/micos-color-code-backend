package dev.sara.micos_color_code.auth;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO request);

    void logout(String token);
}
