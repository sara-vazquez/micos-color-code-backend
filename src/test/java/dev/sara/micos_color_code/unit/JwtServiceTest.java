package dev.sara.micos_color_code.unit;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import dev.sara.micos_color_code.security.JwtService;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private JwtService jwtService;

    private String secretKeyBase64;

    @BeforeEach
    void setUp() throws Exception {
        byte[] keyBytes = new byte[64];
        new java.security.SecureRandom().nextBytes(keyBytes);
        secretKeyBase64 = Base64.getEncoder().encodeToString(keyBytes);

        jwtService = new JwtService(secretKeyBase64, jwtDecoder);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenValid() {
        String fakeToken = "valid.jwt.token";
        UserDetails userDetails = new User("test@example.com", "password", List.of());

        Jwt jwtMock = mock(Jwt.class);
        when(jwtDecoder.decode(fakeToken)).thenReturn(jwtMock);
        when(jwtMock.getSubject()).thenReturn("test@example.com");
        when(jwtMock.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));

        boolean result = jwtService.isTokenValid(fakeToken, userDetails);
        assertTrue(result);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenExpired() {
        String fakeToken = "expired.jwt.token";
        UserDetails userDetails = new User("test@example.com", "password", List.of());

        Jwt jwtMock = mock(Jwt.class);
        when(jwtDecoder.decode(fakeToken)).thenReturn(jwtMock);
        when(jwtMock.getSubject()).thenReturn("test@example.com");
        when(jwtMock.getExpiresAt()).thenReturn(Instant.now().minusSeconds(10));

        boolean result = jwtService.isTokenValid(fakeToken, userDetails);
        assertFalse(result);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenDecoderThrowsException() {
        String badToken = "broken.jwt.token";
        UserDetails userDetails = new User("test@example.com", "password", List.of());

        when(jwtDecoder.decode(badToken)).thenThrow(new JwtException("Invalid token"));

        boolean result = jwtService.isTokenValid(badToken, userDetails);
        assertFalse(result);
    }
}