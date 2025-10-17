package dev.sara.micos_color_code.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

         Jwt jwt = (Jwt) authentication.getPrincipal();
         String email = jwt.getSubject();  

        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(Authentication authentication, @RequestBody UserUpdateDTO updateDTO) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getSubject();

        UserResponseDTO updatedUser = userService.updateProfile(email, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}
