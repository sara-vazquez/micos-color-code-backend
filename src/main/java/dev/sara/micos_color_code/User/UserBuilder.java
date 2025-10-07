package dev.sara.micos_color_code.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserBuilder {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserEntity build(UserRequestDTO request) {
        String encodedPassword = passwordEncoder.encode(request.password());

        return new UserEntity(
            request.username(),
            request.email(),
            encodedPassword
        );
    }

    
}
