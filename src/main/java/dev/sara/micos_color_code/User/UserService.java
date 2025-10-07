package dev.sara.micos_color_code.User;

import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final CaptchaService captchaService;

    private final UserBuilder userBuilder; 

    public UserResponseDTO register(UserRequestDTO request) {
        boolean captchaValid = captchaService.validate(request.captchaId(), request.captchaAnswer());
        if(!captchaValid) {
            throw new IllegalArgumentException("Captcha inválido o expirado");
        }

        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if(userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        UserEntity userToSave = userBuilder.build(request);

        UserEntity savedUser = userRepository.save(userToSave);
        
        return UserMapper.toResponseDTO(savedUser);
    }
}
