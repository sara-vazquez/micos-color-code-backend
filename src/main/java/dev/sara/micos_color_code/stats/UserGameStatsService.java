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
        
        Integer currentTotalPoints = stats.getTotalPoints();
        Integer currentGamesPlayed = stats.getGamesPlayed();
        Integer currentLevelValue = stats.getCurrentLevel();
        
        if (currentTotalPoints == null) currentTotalPoints = 0;
        if (currentGamesPlayed == null) currentGamesPlayed = 0;
        if (currentLevelValue == null) currentLevelValue = 1;
        
        stats.setTotalPoints(currentTotalPoints + pointsToAdd);
        stats.setGamesPlayed(currentGamesPlayed + 1);
        
        if (completed && currentLevelValue < MAX_LEVEL) {
            stats.setCurrentLevel(currentLevelValue + 1);
        } else {
            stats.setCurrentLevel(currentLevelValue);
        }
        
        userGameStatsRepository.save(stats);
        
        return stats.getTotalPoints();
    }

    public RankingResponseDTO getRanking(Long gameId, Long currentUserId) {
        // TOP 3
        Pageable top3Pageable = PageRequest.of(0, 3);
        List<UserGameStatsEntity> top3Stats = userGameStatsRepository
            .findByGame_IdOrderByTotalPointsDesc(gameId, top3Pageable);
        
        List<RankingPlayerDTO> top3 = new ArrayList<>();
        for (int i = 0; i < top3Stats.size(); i++) {
            UserGameStatsEntity stat = top3Stats.get(i);
            top3.add(new RankingPlayerDTO(
                stat.getUser().getUsername(),
                stat.getTotalPoints() != null ? stat.getTotalPoints() : 0,
                i + 1
            ));
        }
        
        // Current user
        UserGameStatsEntity currentUserStats = userGameStatsRepository
            .findByUser_IdAndGame_Id(currentUserId, gameId)
            .orElse(null);
        
        RankingPlayerDTO currentUser = null;
        Integer currentLevel = 1;
        
        if (currentUserStats != null) {
            Integer totalPoints = currentUserStats.getTotalPoints() != null ? currentUserStats.getTotalPoints() : 0;
            
            Long playersAhead = userGameStatsRepository
                .countPlayersWithHigherScore(gameId, totalPoints);
            int position = playersAhead.intValue() + 1;
            
            currentUser = new RankingPlayerDTO(
                currentUserStats.getUser().getUsername(),
                totalPoints,
                position
            );
            
            currentLevel = currentUserStats.getCurrentLevel() != null ? currentUserStats.getCurrentLevel() : 1;
        }
        
        return new RankingResponseDTO(top3, currentUser, currentLevel);
    }
    
    public int getCurrentLevel(Long userId, Long gameId) {
        return userGameStatsRepository
            .findByUser_IdAndGame_Id(userId, gameId)
            .map(stats -> stats.getCurrentLevel() != null ? stats.getCurrentLevel() : 1)
            .orElse(1);
    }
}