package dev.sara.micos_color_code.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Role.RoleEntity;
import dev.sara.micos_color_code.User.UserEntity;
import dev.sara.micos_color_code.User.UserRepository;
import dev.sara.micos_color_code.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

        // look for user in db
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // generate jwt
        String token = jwtService.generateToken(user);

        String role = user.getRoles().stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        return new AuthResponseDTO(token, user.getUsername(), role);
    }

    @Override
    public void logout(String token){}
}