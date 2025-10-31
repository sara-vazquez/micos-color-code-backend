package dev.sara.micos_color_code.integration;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.sara.micos_color_code.captcha.CaptchaService;
import dev.sara.micos_color_code.register.RegisterRequestDTO;
import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.role.RoleRepository;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;
import dev.sara.micos_color_code.util.EmailService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class RegisterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private CaptchaService captchaService;
    
    @MockitoBean
    private EmailService emailService;
    
    private RoleEntity userRole;
    private RoleEntity adminRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> {
                RoleEntity role = new RoleEntity();
                role.setName("ROLE_USER");
                return roleRepository.save(role);
            });

        adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> {
                RoleEntity role = new RoleEntity();
                role.setName("ROLE_ADMIN");
                return roleRepository.save(role);
            });
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserEntity firstUser = new UserEntity();
        firstUser.setUsername("firstuser");
        firstUser.setEmail("first@example.com");
        firstUser.setPassword("password");
        firstUser.setEnabled(true);
        firstUser.setRoles(Set.of(adminRole));
        userRepository.save(firstUser);
    
        RegisterRequestDTO requestDTO = new RegisterRequestDTO();
        requestDTO.setUsername("testuser");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setCaptchaId("captcha-id");
        requestDTO.setCaptchaAnswer(42);
    
        when(captchaService.validate("captcha-id", 42)).thenReturn(true);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString(), anyString());
    
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));
    
        UserEntity savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(savedUser.getUsername(), is(equalTo("testuser")));
        assertThat(savedUser.isEnabled(), is(false));
        assertThat(savedUser.getConfirmationToken(), is(notNullValue()));
        
        verify(emailService).sendConfirmationEmail(
            eq("test@example.com"), 
            eq("testuser"), 
            anyString()
        );
    }

    @Test
    void testRegisterFirstUser_ShouldBeAdmin() throws Exception {
        userRepository.deleteAll();

        RegisterRequestDTO requestDTO = new RegisterRequestDTO();
        requestDTO.setUsername("adminuser");
        requestDTO.setEmail("admin@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setCaptchaId("captcha-id");
        requestDTO.setCaptchaAnswer(42);

        when(captchaService.validate("captcha-id", 42)).thenReturn(true);
        doNothing().when(emailService).sendConfirmationEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("adminuser")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));

        UserEntity savedUser = userRepository.findByEmail("admin@example.com").orElseThrow();
        assertThat(savedUser.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_ADMIN")), is(true));
    }

    @Test
    void testRegisterUser_InvalidCaptcha() throws Exception {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO();
        requestDTO.setUsername("testuser");
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setCaptchaId("captcha-id");
        requestDTO.setCaptchaAnswer(99);

        when(captchaService.validate("captcha-id", 99)).thenReturn(false);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Captcha inválido")));

        assertThat(userRepository.findByEmail("test@example.com").isPresent(), is(false));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }


    @Test
    void testConfirmAccount_InvalidToken() throws Exception {
        mockMvc.perform(get("/register/confirm")
                .param("token", "invalid-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://localhost:5173/login?error=*"));
    }

    @Test
    void testConfirmAccount_ExpiredToken() throws Exception {
        String token = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(false);
        user.setConfirmationToken(token);
        user.setTokenCreationDate(LocalDateTime.now().minusHours(25));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        mockMvc.perform(get("/register/confirm")
                .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://localhost:5173/login?error=*"));

        UserEntity unconfirmedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(unconfirmedUser.isEnabled(), is(false));
    }

    @Test
    void testResendConfirmation_AlreadyVerified() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        mockMvc.perform(post("/register/resend-confirmation")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("La cuenta ya está verificada")));

        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testResendConfirmation_UserNotFound() throws Exception {
        mockMvc.perform(post("/register/resend-confirmation")
                .param("email", "nonexistent@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Usuario no encontrado")));
        
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString(), anyString());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }
}