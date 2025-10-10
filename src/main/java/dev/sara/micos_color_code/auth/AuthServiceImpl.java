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
        System.out.println("🔍 Intentando login con email: " + request.getEmail());

        try {// Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
            System.out.println("✅ Autenticación exitosa");

        } catch (Exception e) {
            System.out.println("❌ Error en autenticación: " + e.getMessage());
            throw e;
        }

        // look for user in db
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            System.out.println("✅ Usuario encontrado: " + user.getUsername());
            System.out.println("🔑 Password hasheada en BD: " + user.getPassword());
            System.out.println("🔑 Password recibida: " + request.getPassword());

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