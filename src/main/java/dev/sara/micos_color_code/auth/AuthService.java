package dev.sara.micos_color_code.auth;

import dev.sara.micos_color_code.register.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthRequestDTO login(AuthRequestDTO request);
}
