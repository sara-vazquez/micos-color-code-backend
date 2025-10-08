package dev.sara.micos_color_code.auth;

import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.register.RegisterRequestDTO;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        return new AuthResponseDTO("token", "username", "ROLE_USER");
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        return new AuthResponseDTO("token", "username", "ROLE_USER");
    }

    @Override
    public void logout(String token){}
}
