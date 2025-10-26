package dev.sara.micos_color_code.stats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sara.micos_color_code.play.GameEntity;
import dev.sara.micos_color_code.play.GameNotFoundException;
import dev.sara.micos_color_code.play.GameRepository;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;

@Service
public class UserGameStatsService {

    private final UserGameStatsRepository userGameStatsRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    private static final int MAX_LEVEL = 4;

    public UserGameStatsService(UserGameStatsRepository userGameStatsRepository, UserRepository userRepository, GameRepository gameRepository) {
        this.userGameStatsRepository = userGameStatsRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public int updateUserStats(Long userId, Long gameId, int pointsToAdd, int levelPlayed, boolean completed) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new GameNotFoundException("Usuario no encontrado"));
        GameEntity game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException("Juego no encontrado"));
        
        UserGameStatsEntity stats = userGameStatsRepository
            .findByUser_IdAndGame_Id(userId, gameId)
            .orElse(UserGameStatsEntity.builder()
                .user(user)
                .game(game)
                .totalPoints(0)
                .gamesPlayed(0)
                .currentLevel(1)
                .build());

        stats.setTotalPoints(stats.getTotalPoints() + pointsToAdd);
        stats.setGamesPlayed(stats.getGamesPlayed() + 1);
        
        if (completed && stats.getCurrentLevel() < MAX_LEVEL) {
            stats.setCurrentLevel(stats.getCurrentLevel() + 1);
        }
        
        userGameStatsRepository.save(stats);
        
        return stats.getTotalPoints();
    }

    // TOP 3
    public RankingResponseDTO getRanking(Long gameId, Long currentUserId) {
        Pageable top3Pageable = PageRequest.of(0, 3);
        List<UserGameStatsEntity> top3Stats = userGameStatsRepository
            .findByGame_IdOrderByTotalPointsDesc(gameId, top3Pageable);
        
        List<RankingPlayerDTO> top3 = new ArrayList<>();
        for (int i = 0; i < top3Stats.size(); i++) {
            UserGameStatsEntity stat = top3Stats.get(i);
            top3.add(new RankingPlayerDTO(
                stat.getUser().getUsername(),
                stat.getTotalPoints(),
                i + 1
            ));
        }

        //Current user
        UserGameStatsEntity currentUserStats = userGameStatsRepository
            .findByUser_IdAndGame_Id(currentUserId, gameId)
            .orElse(null);
        
        RankingPlayerDTO currentUser = null;
        int currentLevel = 1; 

        if (currentUserStats != null) {
            Long playersAhead = userGameStatsRepository
                .countPlayersWithHigherScore(gameId, currentUserStats.getTotalPoints());
            int position = playersAhead.intValue() + 1;
            
            currentUser = new RankingPlayerDTO(
                currentUserStats.getUser().getUsername(),
                currentUserStats.getTotalPoints(),
                position
            );

            currentLevel = currentUserStats.getCurrentLevel();
        }
        
        return new RankingResponseDTO(top3, currentUser, currentLevel);
    }

    /* Gets user current level in a game */
    public int getCurrentLevel(Long userId, Long gameId) {
        return userGameStatsRepository
            .findByUser_IdAndGame_Id(userId, gameId)
            .map(UserGameStatsEntity::getCurrentLevel)
            .orElse(1);
    }
}
