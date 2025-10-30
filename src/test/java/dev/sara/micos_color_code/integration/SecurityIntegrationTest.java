package dev.sara.micos_color_code.integration;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import dev.sara.micos_color_code.auth.AuthService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"jwt.key=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcw=="})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private String validUserToken;
    private String validAdminToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        validUserToken = generateToken("user123", List.of("ROLE_USER"), false);
        
        validAdminToken = generateToken("admin123", List.of("ROLE_ADMIN"), false);
        
        expiredToken = generateToken("user123", List.of("ROLE_USER"), true);
    }


    @Test
    void publicEndpoints_ShouldBeAccessible_WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/captcha/generate"))
                .andExpect(status().isOk());
    }

    @Test
    void uploads_ShouldBeAccessible_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/uploads/image.jpg"))
                .andExpect(status().isNotFound());
    }


    @Test
    void protectedEndpoints_ShouldReject_WithoutToken() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldAllow_WithValidToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_ShouldReject_ExpiredToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldReject_InvalidFormatToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldReject_TokenWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", validUserToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_ShouldReject_UserWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_ShouldAllow_UserWithAdminRole() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    void userEndpoint_ShouldAllow_UserWithUserRole() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk());
    }

    @Test
    void userEndpoint_ShouldAllow_UserWithAdminRole() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }


    @Test
    void response_ShouldInclude_CorsHeaders() throws Exception {
        mockMvc.perform(options("/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void api_ShouldNotRequire_CsrfToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_ShouldRequire_Authentication() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_ShouldSucceed_WithValidToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + validUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
    }

    @Test
    void feedback_ShouldBeAccessible_WithoutAuthentication() throws Exception {
        mockMvc.perform(post("/feedback/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"test feedback\"}"))
                .andExpect(status().isOk());
    }

    private String generateToken(String subject, List<String> roles, boolean expired) {
        String secretKey = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcw==";
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");

        Instant now = Instant.now();
        Instant expiration = expired ? now.minusSeconds(3600) : now.plusSeconds(3600);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(expiration)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        
        JwtEncoder encoder = new NimbusJwtEncoder(
            new ImmutableSecret<>(key)
        );

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}