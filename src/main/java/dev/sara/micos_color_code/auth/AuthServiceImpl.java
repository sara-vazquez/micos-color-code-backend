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

        // Authenticate user
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            System.out.println("✅ Autenticación exitosa");
        } catch (Exception e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
            throw new RuntimeException("Credenciales incorrectas");
        }

        // Find user in db if exists
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("✅ Usuario encontrado: " + user.getUsername());

        //Generate token with JwtService
        String token = jwtService.generateToken(user);

        String role = user.getRoles().stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        System.out.println("✅ Token generado correctamente");

        return new AuthResponseDTO(token, user.getUsername(), role);
    }

    @Override
    public void logout(String token) {}
}
