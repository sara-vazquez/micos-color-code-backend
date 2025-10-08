package dev.sara.micos_color_code.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Role.RoleRepository;
import dev.sara.micos_color_code.User.UserRepository;
import dev.sara.micos_color_code.register.RegisterRequestDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; 
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        return new AuthResponseDTO("token", "username", "ROLE_USER");
    }

    @Override
    public void logout(String token){}

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }
    }
}
