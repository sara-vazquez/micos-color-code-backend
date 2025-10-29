package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.sara.micos_color_code.register.RegisterRequestDTO;
import dev.sara.micos_color_code.register.RegisterResponseDTO;
import dev.sara.micos_color_code.register.RegisterServiceImpl;
import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.role.RoleRepository;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import dev.sara.micos_color_code.util.EmailService;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class RegisterServiceImplTest {

    @InjectMocks
    private RegisterServiceImpl registerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private RegisterRequestDTO requestDTO;
    private UserEntity userEntity;
    private RoleEntity userRole;
    private RoleEntity adminRole;

    @BeforeEach
    void setUp() {
        requestDTO = new RegisterRequestDTO();
        requestDTO.setUsername("maurihidalgo");
        requestDTO.setEmail("mauriciohidalgo@gmail.com");
        requestDTO.setPassword("password123");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("maurihidalgo");
        userEntity.setEmail("mauriciohidalgo@gmail.com");
        userEntity.setPassword("hashed-password");
        userEntity.setEnabled(false);

        adminRole = new RoleEntity();
        adminRole.setId_role(1L);
        adminRole.setName("ROLE_ADMIN");

        userRole = new RoleEntity();
        userRole.setId_role(2L);
        userRole.setName("ROLE_USER");
    }
    
     @Test
    void register_ShouldRegisterUser_WhenDataIsValidAndNotFirstUser() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(5L);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        RegisterResponseDTO result = registerService.register(requestDTO);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("maurihidalgo") &&
                user.getEmail().equals("mauriciohidalgo@gmail.com") &&
                user.getPassword().equals("hashed-password") &&
                !user.isEnabled() &&
                user.getConfirmationToken() != null &&
                user.getTokenCreationDate() != null &&
                user.getRoles().contains(userRole)
        ));
        verify(emailService).sendConfirmationEmail(
                eq("mauriciohidalgo@gmail.com"),
                eq("maurihidalgo"),
                anyString()
        );
        
        assertThat(result.getUsername(), is(equalTo("maurihidalgo")));
        assertThat(result.getRole(), is(equalTo("ROLE_USER")));
    }

    @Test
    void register_ShouldRegisterAdminUser_WhenFirstUser() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        RegisterResponseDTO result = registerService.register(requestDTO);

        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(roleRepository, never()).findByName("ROLE_USER");
        verify(userRepository).save(argThat(user ->
                user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"))
        ));
        
        assertThat(result.getRole(), is(equalTo("ROLE_ADMIN")));
    }

    @Test
    void register_ShouldGenerateConfirmationToken_WhenUserIsRegistered() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(1L);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        registerService.register(requestDTO);

        verify(userRepository).save(argThat(user ->
                user.getConfirmationToken() != null &&
                user.getTokenCreationDate() != null &&
                !user.isEnabled()
        ));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com"))
                .thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("Email ya registrado"));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("Username ya registrado"));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void register_ShouldThrowException_WhenUserRoleNotFound() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(1L);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("Rol USER no encontrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenAdminRoleNotFound() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.register(requestDTO)
        );

        assertThat(exception.getMessage(), is("Rol ADMIN no encontrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldEncodePassword_WhenUserIsRegistered() {
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("maurihidalgo")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(1L);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("super-secure-hash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        registerService.register(requestDTO);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("super-secure-hash")
        ));
    }

    @Test
    void confirmAccount_ShouldActivateAccount_WhenTokenIsValid() {
        String token = UUID.randomUUID().toString();
        userEntity.setConfirmationToken(token);
        userEntity.setTokenCreationDate(LocalDateTime.now().minusHours(1));
        userEntity.setEnabled(false);

        when(userRepository.findByConfirmationToken(token)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        registerService.confirmAccount(token);

        verify(userRepository).save(argThat(user ->
                user.isEnabled() &&
                user.getConfirmationToken() == null &&
                user.getTokenCreationDate() == null
        ));
    }

    @Test
    void confirmAccount_ShouldThrowException_WhenTokenIsInvalid() {
        String token = "invalid-token";
        when(userRepository.findByConfirmationToken(token)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.confirmAccount(token)
        );

        assertThat(exception.getMessage(), is("Token inválido"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmAccount_ShouldThrowException_WhenTokenIsExpired() {
        String token = UUID.randomUUID().toString();
        userEntity.setConfirmationToken(token);
        userEntity.setTokenCreationDate(LocalDateTime.now().minusHours(25));
        userEntity.setEnabled(false);

        when(userRepository.findByConfirmationToken(token)).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(RuntimeException.class, () ->
                registerService.confirmAccount(token)
        );

        assertThat(exception.getMessage(), is("El token ha expirado"));
        verify(userRepository, never()).save(any());
    }
}
