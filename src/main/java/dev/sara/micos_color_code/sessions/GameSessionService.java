package dev.sara.micos_color_code.sessions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sara.micos_color_code.play.GameNotFoundException;
import dev.sara.micos_color_code.play.GameRepository;
import dev.sara.micos_color_code.stats.UserGameStatsRepository;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;

@Service
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final GameRepository gameRepository;
    private final UserGameStatsRepository userGameStatsRepository;
    private final UserRepository userRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository, GameRepository gameRepository, UserGameStatsRepository userGameStatsRepository, UserRepository userRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.gameRepository = gameRepository;
        this.userGameStatsRepository = userGameStatsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameSessionResponseDTO completeGameSession(Long userId, Long gameId, GameSessionRequestDTO gameSessionRequestDTO) {
        UserEntity user = userRepository.findById(userId);
            .orElseThrow(() -> new GameNotFoundException("Usuario no encontrado"));

        GameEntity game = gameRepository.findById(gameId);
            .orElseThrow(() -> new GameNotFoundException("Juego no encontrado"));

        GameSessionEntity session = GameSessionEntity.builder() //save session
            .user(user);
            .game(game);
            .points(request.points())
            .timeCompleted(request.timeCompleted())
            .build();
        gameSessionRepository.save(session);

        //updates Ranking
        int newTotalPoints = userGameStatsService.updateStats(userId, gameId, request.points());
        return new GameSessionResponseDTO(request.points(), newTotalPoints);
    }
    
}
