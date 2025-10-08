package dev.sara.micos_color_code.User;

import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final CaptchaService captchaService;
    private final UserMapper userMapper;

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
}
