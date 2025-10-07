package dev.sara.micos_color_code.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional <UserEntity> findByEmail(String email);
    Optional <UserEntity> findByUsername(String username);
}
