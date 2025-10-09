package dev.sara.micos_color_code.auth;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Role.RoleEntity;
import dev.sara.micos_color_code.Role.RoleRepository;
import dev.sara.micos_color_code.User.UserEntity;
import dev.sara.micos_color_code.User.UserRepository;
import dev.sara.micos_color_code.register.RegisterRequestDTO;
import dev.sara.micos_color_code.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; 
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // look for user in db
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // generate jwt
        String token = jwtService.generateToken(user);

        // get main role
        String role = user.getRoles().stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        return new AuthResponseDTO(token, user.getUsername(), role);
    }

    @Override
    public void logout(String token){}

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Validate if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }

        // Validate if username exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username ya registrado");
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Encrypt password -BCrypt
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set roles
        Set<RoleEntity> roles = new HashSet<>();
        
        // If it's first user = admin
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

        // Save on db
        UserEntity savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtService.generateToken(savedUser);

        // Get role name
        String roleName = roles.stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        return new AuthResponseDTO(token, savedUser.getUsername(), roleName);
    }
}