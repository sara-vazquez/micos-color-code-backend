package dev.sara.micos_color_code.register;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Role.RoleEntity;
import dev.sara.micos_color_code.Role.RoleRepository;
import dev.sara.micos_color_code.User.UserEntity;
import dev.sara.micos_color_code.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

        String roleName = roles.stream()
            .map(RoleEntity::getName)
            .findFirst()
            .orElse("ROLE_USER");

        return new RegisterResponseDTO(savedUser.getUsername(), roleName);
    }
    
}
