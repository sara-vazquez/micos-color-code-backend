package dev.sara.micos_color_code.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserEntity toEntity(UserRequestDTO dto) {
        return UserEntity.builder()
        .username(dto.username())
        .email(dto.email())
        .password(passwordEncoder.encode(dto.password()))
        .build();
    }

    public UserResponseDTO toResponse(UserEntity entity) {
        return new UserResponseDTO(entity.getId(), entity.getUsername(), entity.getEmail());
    }
}
