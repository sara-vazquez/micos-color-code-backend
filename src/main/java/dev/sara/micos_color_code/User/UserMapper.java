package dev.sara.micos_color_code.User;

public class UserMapper {
    
    public static UserResponseDTO toResponseDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new UserResponseDTO(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail()
        );
    }
}
