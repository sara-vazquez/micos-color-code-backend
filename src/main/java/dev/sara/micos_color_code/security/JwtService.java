package dev.sara.micos_color_code.security;

import java.time.Instant;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import dev.sara.micos_color_code.User.UserEntity;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(@Value("${jwt.key}") String secretKeyBase64, JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;

        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");

        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("micos_color_code")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractUsername(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        Instant expiration = jwtDecoder.decode(token).getExpiresAt();
        return expiration != null && expiration.isBefore(Instant.now());
    }
}
