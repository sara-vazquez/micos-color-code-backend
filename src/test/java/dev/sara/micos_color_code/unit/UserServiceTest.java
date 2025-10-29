package dev.sara.micos_color_code.unit;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sara.micos_color_code.captcha.CaptchaService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserMapper;
import dev.sara.micos_color_code.user.UserRepository;
import dev.sara.micos_color_code.user.UserRequestDTO;
import dev.sara.micos_color_code.user.UserResponseDTO;
import dev.sara.micos_color_code.user.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    private UserRequestDTO requestDTO;
    private UserEntity userEntity;
    private UserResponseDTO responseDTO;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {

        requestDTO = new UserRequestDTO("maurihidalgo", "mauriciohidalgo@gmail.com", "password123", "6438992074jkilwn234", 24);

        userEntity = new UserEntity();
        userEntity.setUsername("maurihidalgo");
        userEntity.setEmail("mauriciohidalgo@gmail.com");
        userEntity.setPassword("hashed-password");

        responseDTO = new UserResponseDTO(1L, "maurihidalgo", "mauriciohidalgo@gmail.com");
    }

    @Test
    void register_ShouldRegisterUser_WhenDataIsValid() {
        when(captchaService.validate("6438992074jkilwn234", 24)).thenReturn(true);
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestDTO)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(responseDTO);

        UserResponseDTO result = userService.register(requestDTO);

        verify(userRepository).save(userEntity);
        assertThat(result.username(), is(equalTo("maurihidalgo")));
        assertThat(result.email(), is(equalTo("mauriciohidalgo@gmail.com")));
    }

    @Test
    void register_ShouldThrowException_WhenCaptchaInvalid() {
    when(captchaService.validate(anyString(), anyInt())).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("Captcha inválido o expirado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(captchaService.validate(anyString(), anyInt())).thenReturn(true);
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("El correo ya está registrado"));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(captchaService.validate(anyString(), anyInt())).thenReturn(true);
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("El nombre de usuario ya existe"));
    }

}