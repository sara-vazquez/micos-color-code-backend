package dev.sara.micos_color_code.unitTests;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import dev.sara.micos_color_code.Captcha.CaptchaService;
import dev.sara.micos_color_code.User.UserEntity;
import dev.sara.micos_color_code.User.UserMapper;
import dev.sara.micos_color_code.User.UserRepository;
import dev.sara.micos_color_code.User.UserRequestDTO;
import dev.sara.micos_color_code.User.UserResponseDTO;
import dev.sara.micos_color_code.User.UserService;

public class UserServiceTest {

    @MockitoBean
    private UserRepository userRepository;
    
    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private UserMapper userMapper;
    
    @InjectMocks
    private UserService userService;

    private UserRequestDTO requestDTO;
    private UserEntity userEntity;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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