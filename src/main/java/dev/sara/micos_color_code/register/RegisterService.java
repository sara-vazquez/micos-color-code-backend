package dev.sara.micos_color_code.register;

import org.springframework.stereotype.Service;

@Service
public interface RegisterService {
    RegisterResponseDTO register(RegisterRequestDTO request);
    void confirmAccount(String token);
}