package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.sara.micos_color_code.play.GameEntity;
import dev.sara.micos_color_code.play.GameNotFoundException;
import dev.sara.micos_color_code.play.GameRepository;
import dev.sara.micos_color_code.sessions.GameSessionEntity;
import dev.sara.micos_color_code.sessions.GameSessionRepository;
import dev.sara.micos_color_code.sessions.GameSessionRequestDTO;
import dev.sara.micos_color_code.sessions.GameSessionResponseDTO;
import dev.sara.micos_color_code.sessions.GameSessionService;
import dev.sara.micos_color_code.stats.UserGameStatsService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GameSessionServiceTest {
    
    @InjectMocks
    private GameSessionService gameSessionService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameStatsService userGameStatsService;
    
    private UserEntity userEntity;
    private GameEntity gameEntity;
    private GameSessionRequestDTO requestDTO;
    private GameSessionEntity gameSessionEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("maurihidalgo");

        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameName("Memory Game");

        requestDTO = new GameSessionRequestDTO(100, 45, 2, true);

        gameSessionEntity = GameSessionEntity.builder()
                .user(userEntity)
                .game(gameEntity)
                .points(100)
                .timeCompleted(45)
                .levels(2)
                .build();
    }

       @Test
        void completeGameSession_ShouldCreateSessionAndReturnResponse_WhenDataIsValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsService.updateUserStats(1L, 1L, 100, 2, true)).thenReturn(500);
        when(userGameStatsService.getCurrentLevel(1L, 1L)).thenReturn(3);

        GameSessionResponseDTO result = gameSessionService.completeGameSession(1L, 1L, requestDTO);

        verify(gameSessionRepository).save(argThat(session ->
                session.getUser().getId().equals(1L) &&
                session.getGame().getId().equals(1L) &&
                session.getPoints() == 100 &&
                session.getTimeCompleted() == 45 &&
                session.getLevels() == 2
        ));
        verify(userGameStatsService).updateUserStats(1L, 1L, 100, 2, true);
        verify(userGameStatsService).getCurrentLevel(1L, 1L);
        
        assertThat(result.sessionPoints(), is(equalTo(100)));
        assertThat(result.newTotalPoints(), is(equalTo(500)));
        assertThat(result.currentLevel(), is(equalTo(3)));
    }

    @Test
    void completeGameSession_ShouldHandleNotCompletedGame_WhenCompletedIsFalse() {
        GameSessionRequestDTO notCompletedRequest = new GameSessionRequestDTO(50, 30, 1, false);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsService.updateUserStats(1L, 1L, 50, 1, false)).thenReturn(350);
        when(userGameStatsService.getCurrentLevel(1L, 1L)).thenReturn(2);

        GameSessionResponseDTO result = gameSessionService.completeGameSession(1L, 1L, notCompletedRequest);

        verify(userGameStatsService).updateUserStats(1L, 1L, 50, 1, false);
        assertThat(result.sessionPoints(), is(equalTo(50)));
        assertThat(result.newTotalPoints(), is(equalTo(350)));
        assertThat(result.currentLevel(), is(equalTo(2)));
    }

    @Test
    void completeGameSession_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(GameNotFoundException.class, () ->
                gameSessionService.completeGameSession(1L, 1L, requestDTO)
        );

        assertThat(exception.getMessage(), is("Usuario no encontrado"));
        verify(gameSessionRepository, never()).save(any());
        verify(userGameStatsService, never()).updateUserStats(anyLong(), anyLong(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void completeGameSession_ShouldThrowException_WhenGameNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(GameNotFoundException.class, () ->
                gameSessionService.completeGameSession(1L, 1L, requestDTO)
        );

        assertThat(exception.getMessage(), is("Juego no encontrado"));
        verify(gameSessionRepository, never()).save(any());
        verify(userGameStatsService, never()).updateUserStats(anyLong(), anyLong(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void completeGameSession_ShouldHandleDifferentLevels_WhenLevelVaries() {
        GameSessionRequestDTO level4Request = new GameSessionRequestDTO(200, 90, 4, true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsService.updateUserStats(1L, 1L, 200, 4, true)).thenReturn(700);
        when(userGameStatsService.getCurrentLevel(1L, 1L)).thenReturn(4);

        GameSessionResponseDTO result = gameSessionService.completeGameSession(1L, 1L, level4Request);

        verify(gameSessionRepository).save(argThat(session ->
                session.getLevels() == 4
        ));
        verify(userGameStatsService).updateUserStats(1L, 1L, 200, 4, true);
        assertThat(result.currentLevel(), is(equalTo(4)));
    }
    
}
