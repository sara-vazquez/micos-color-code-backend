package dev.sara.micos_color_code.play;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    Optional<GameEntity> findByName(String gameName);
    boolean existsByName(String gameName);
    
}
