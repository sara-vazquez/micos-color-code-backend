package dev.sara.micos_color_code.play;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    // Needed for other services to verify that game exists
}
