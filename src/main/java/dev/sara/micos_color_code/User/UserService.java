package dev.sara.micos_color_code.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Register
    public UserResponseDTO register(UserRequestDTO dto) {
        if(!captchaService.validate(dto.captchaId(), dto.captchaAnswer())) {
            throw new IllegalArgumentException("Captcha inválido o expirado");
        }

        if(userRepository.findByEmail(dto.email()).isPresent()){
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if(userRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        UserEntity user = userMapper.toEntity(dto);
        userRepository.save(user);

        return userMapper.toResponse(user);

    }

    // Edit profile
    public UserResponseDTO updateProfile(UserDetails userDetails, UserUpdateDTO dto) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.username() != null && !dto.username().isBlank())
            user.setUsername(dto.username());
        if (dto.email() != null && !dto.email().isBlank())
            user.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isBlank())
            user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
