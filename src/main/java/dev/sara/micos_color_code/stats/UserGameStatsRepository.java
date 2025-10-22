package dev.sara.micos_color_code.stats;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserGameStatsRepository extends JpaRepository<UserGameStatsEntity, Long>{
    List<UserGameStatsEntity> findByGame_IdOrderByTotalPointsDesc(Long gameId, Pageable pageable); // get the TOP 3
    Optional<UserGameStatsEntity> findByUser_IdAndGame_Id(Long userId, Long gameId); // get data from current user
    
    @Query("SELECT COUNT(s) FROM UserGameStatsEntity s WHERE s.game.id = :gameId AND s.totalPoints > :totalPoints") // finds how many players have a higher position
    Long countPlayersWithHigherScore(@Param("gameId") Long gameId, @Param("totalPoints") int totalPoints); //get current user position
}
