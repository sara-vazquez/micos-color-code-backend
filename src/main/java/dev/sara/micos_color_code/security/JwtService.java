package dev.sara.micos_color_code.security;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import dev.sara.micos_color_code.User.UserEntity;

@Service
public class JwtService {

    private final NimbusJwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(@Value("${jwt.key}") String secretKeyBase64, JwtDecoder jwtDecoder) {
        System.out.println("🔑 JWT_KEY recibida: [" + secretKeyBase64 + "]");
        
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKeyBase64);
            System.out.println("✅ Clave decodificada, tamaño: " + keyBytes.length + " bytes");
            
            // IMPORTANTE: Usar keyID para que Spring Security pueda seleccionar la clave
            JWK jwk = new OctetSequenceKey.Builder(keyBytes)
                .algorithm(JWSAlgorithm.HS512)
                .keyID("micos-key") // ← AÑADE ESTO
                .build();
            
            System.out.println("✅ JWK creado: " + jwk.getAlgorithm());
            
            JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
            this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
            this.jwtDecoder = jwtDecoder;
            
            System.out.println("✅ NimbusJwtEncoder inicializado correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error inicializando JWT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inicializando JWT", e);
        }
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("micos_color_code")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1 hora
                .subject(user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String extractUsername(String token) {
        try {
            return jwtDecoder.decode(token).getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        try {
            Instant expiration = jwtDecoder.decode(token).getExpiresAt();
            return expiration != null && expiration.isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }
}