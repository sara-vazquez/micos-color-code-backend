package dev.sara.micos_color_code.jwt;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
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

    @Value("${jwt.key}")
    private String jwtSecretKey; 

    public JwtService(@Value("${jwt.key}") String secretKeyBase64) {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKeyBase64);
        
        JWK jwk = new OctetSequenceKey.Builder(keyBytes)
                .algorithm(JWSAlgorithm.HS512)
                .build();
        
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tu-app")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // one hour
                .subject(user.getUsername())
                .claim("roles", user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toList()))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}