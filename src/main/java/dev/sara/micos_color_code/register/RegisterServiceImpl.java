package dev.sara.micos_color_code.register;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.role.RoleRepository;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import dev.sara.micos_color_code.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Transactional
    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username ya registrado");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        // Generate confirmation token
        String confirmationToken = UUID.randomUUID().toString();
        user.setConfirmationToken(confirmationToken);
        user.setTokenCreationDate(LocalDateTime.now());

        //First registered user will be the admin
        Set<RoleEntity> roles = new HashSet<>();
        if (userRepository.count() == 0) {
            RoleEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            roles.add(adminRole);
        } else {
            RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);

        //Send confirmation email
        try {
            emailService.sendConfirmationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                confirmationToken
            );
            log.info("Email de confirmación enviado a: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación: {}", e.getMessage());
            // User is saved but doesnt get the mail.
        }

        String roleName = roles.stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        return new RegisterResponseDTO(savedUser.getUsername(), roleName);
    }

    @Transactional
    public void confirmAccount(String token) {
        UserEntity user = userRepository.findByConfirmationToken(token)
            .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (user.getTokenCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        //Activate account
        user.setEnabled(true);
        user.setConfirmationToken(null);
        user.setTokenCreationDate(null);
        
        userRepository.save(user);
        log.info("Cuenta confirmada exitosamente: {}", user.getEmail());
    }
}
