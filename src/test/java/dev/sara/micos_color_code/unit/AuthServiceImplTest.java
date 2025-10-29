package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import dev.sara.micos_color_code.auth.AuthRequestDTO;
import dev.sara.micos_color_code.auth.AuthResponseDTO;
import dev.sara.micos_color_code.auth.AuthServiceImpl;
import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.security.JwtService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class AuthServiceImplTest {
    
    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthRequestDTO authRequestDTO;
    private UserEntity userEntity;
    private RoleEntity userRole;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setEmail("mauriciohidalgo@gmail.com");
        authRequestDTO.setPassword("password123");

        userRole = new RoleEntity();
        userRole.setName("ROLE_USER");

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("maurihidalgo");
        userEntity.setEmail("mauriciohidalgo@gmail.com");
        userEntity.setPassword("hashed-password");
        userEntity.setEnabled(true);
        userEntity.setRoles(roles);

        authentication = mock(Authentication.class);
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com"))
            .thenReturn(Optional.of(userEntity));
        when(jwtService.generateToken(userEntity)).thenReturn("jwt-token-123");

        AuthResponseDTO result = authService.login(authRequestDTO);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("mauriciohidalgo@gmail.com");
        verify(jwtService).generateToken(userEntity);

        assertThat(result.getToken(), is(equalTo("jwt-token-123")));
        assertThat(result.getUsername(), is(equalTo("maurihidalgo")));
        assertThat(result.getRole(), is(equalTo("ROLE_USER")));
    }   

    @Test
    void login_ShouldReturnAdminRole_WhenUserIsAdmin() {
        RoleEntity adminRole = new RoleEntity();
        adminRole.setName("ROLE_ADMIN");
    
        Set<RoleEntity> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        userEntity.setRoles(adminRoles);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
         when(userRepository.findByEmail("mauriciohidalgo@gmail.com"))
            .thenReturn(Optional.of(userEntity));
        when(jwtService.generateToken(userEntity)).thenReturn("jwt-token-admin");

        AuthResponseDTO result = authService.login(authRequestDTO);

        assertThat(result.getRole(), is(equalTo("ROLE_ADMIN")));
    }

    @Test
    void login_ShouldThrowException_WhenAccountIsNotVerified() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account disabled"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                authService.login(authRequestDTO)
        );

        assertThat(exception.getMessage(), 
                is("Tu cuenta aún no ha sido verificada. Revisa tu correo para confirmarla."));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreIncorrect() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                authService.login(authRequestDTO)
        );

        assertThat(exception.getMessage(), is("Email o contraseña incorrectos"));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication error"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                authService.login(authRequestDTO)
        );

        assertThat(exception.getMessage(), is("Error al iniciar sesión. Inténtalo de nuevo."));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFoundInDatabase() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("mauriciohidalgo@gmail.com"))
                .thenReturn(Optional.empty());

            Exception exception = assertThrows(RuntimeException.class, () -> authService.login(authRequestDTO));

        assertThat(exception.getMessage(), is("Usuario no encontrado"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_ShouldDoNothing_WhenCalled() {
        assertDoesNotThrow(() -> authService.logout("any-token"));
    }
}
