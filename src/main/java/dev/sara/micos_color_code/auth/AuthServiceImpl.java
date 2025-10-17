package dev.sara.micos_color_code.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.security.JwtService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        log.info("🔍 Intentando login con email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            log.info("✅ Autenticación exitosa");
        } catch (DisabledException e) {
            log.error("❌ Cuenta no verificada: {}", request.getEmail());
            throw new RuntimeException("Tu cuenta aún no ha sido verificada. Revisa tu correo para confirmarla.");
        } catch (BadCredentialsException e) {
            log.error("❌ Credenciales incorrectas: {}", request.getEmail());
            throw new RuntimeException("Email o contraseña incorrectos");
        } catch (Exception e) {
            log.error("❌ Error en autenticación: {}", e.getMessage());
            throw new RuntimeException("Error al iniciar sesión. Inténtalo de nuevo.");
        }

        // Find user in db if exists
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        log.info("✅ Usuario encontrado: {}", user.getUsername());

        //Generate token with JwtService
        String token = jwtService.generateToken(user);

        String role = user.getRoles().stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        log.info("✅ Token generado correctamente para: {}", user.getUsername());

        return new AuthResponseDTO(token, user.getUsername(), role);
    }

    @Override
    public void logout(String token) {}
}
