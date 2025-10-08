package dev.sara.micos_color_code.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import dev.sara.micos_color_code.Role.RoleEntity;
import dev.sara.micos_color_code.User.UserEntity;
import lombok.Value;

@Service
public class JwtService {

    @Value("${jwt.key}")
    private String jwtSecretKey;

    public String generateToken(UserEntity user) {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtSecretKey);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");

        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.HOURS); // token valid for an hour

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoles().stream()
                        .map(RoleEntity::getName)
                        .toList())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    
}
