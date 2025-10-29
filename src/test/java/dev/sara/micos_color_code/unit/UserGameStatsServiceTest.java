package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.mockito.Mock;

import dev.sara.micos_color_code.play.GameEntity;
import dev.sara.micos_color_code.play.GameNotFoundException;
import dev.sara.micos_color_code.play.GameRepository;
import dev.sara.micos_color_code.stats.RankingResponseDTO;
import dev.sara.micos_color_code.stats.UserGameStatsEntity;
import dev.sara.micos_color_code.stats.UserGameStatsRepository;
import dev.sara.micos_color_code.stats.UserGameStatsService;
import dev.sara.micos_color_code.user.UserEntity;
import dev.sara.micos_color_code.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserGameStatsServiceTest {
    @InjectMocks
    private UserGameStatsService userGameStatsService;

    @Mock
    private UserGameStatsRepository userGameStatsRepository;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    private UserEntity userEntity;
    private GameEntity gameEntity;
    private UserGameStatsEntity statsEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("maurihidalgo");

        gameEntity = new GameEntity();
        gameEntity.setId(1L);

        statsEntity = UserGameStatsEntity.builder()
                .user(userEntity)
                .game(gameEntity)
                .totalPoints(100)
                .gamesPlayed(5)
                .currentLevel(2)
                .build();
    }


    @Test
    void updateUserStats_ShouldCreateNewStats_WhenStatsDoNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L)).thenReturn(Optional.empty());

        int result = userGameStatsService.updateUserStats(1L, 1L, 50, 1, false);

        verify(userGameStatsRepository).save(argThat(stats ->
                stats.getTotalPoints() == 50 &&
                stats.getGamesPlayed() == 1 &&
                stats.getCurrentLevel() == 1
        ));
        assertThat(result, is(equalTo(50)));
    }

    @Test
    void updateUserStats_ShouldUpdateExistingStats_WhenStatsExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L)).thenReturn(Optional.of(statsEntity));

        int result = userGameStatsService.updateUserStats(1L, 1L, 30, 2, false);

        verify(userGameStatsRepository).save(argThat(stats ->
                stats.getTotalPoints() == 130 &&
                stats.getGamesPlayed() == 6 &&
                stats.getCurrentLevel() == 2
        ));
        assertThat(result, is(equalTo(130)));
    }

    @Test
    void updateUserStats_ShouldIncrementLevel_WhenCompletedAndNotMaxLevel() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L)).thenReturn(Optional.of(statsEntity));

        int result = userGameStatsService.updateUserStats(1L, 1L, 50, 2, true);

        verify(userGameStatsRepository).save(argThat(stats ->
                stats.getCurrentLevel() == 3
        ));
        assertThat(result, is(equalTo(150)));
    }

    @Test
    void updateUserStats_ShouldNotIncrementLevel_WhenNotCompleted() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L)).thenReturn(Optional.of(statsEntity));

        userGameStatsService.updateUserStats(1L, 1L, 50, 2, false);

        verify(userGameStatsRepository).save(argThat(stats ->
                stats.getCurrentLevel() == 2
        ));
    }

    @Test
    void updateUserStats_ShouldNotIncrementLevel_WhenAlreadyMaxLevel() {
        statsEntity.setCurrentLevel(4);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(gameEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L)).thenReturn(Optional.of(statsEntity));

        userGameStatsService.updateUserStats(1L, 1L, 50, 4, true);

        verify(userGameStatsRepository).save(argThat(stats ->
                stats.getCurrentLevel() == 4
        ));
    }

    @Test
    void updateUserStats_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(GameNotFoundException.class, () ->
                userGameStatsService.updateUserStats(1L, 1L, 50, 1, false)
        );

        assertThat(exception.getMessage(), is("Usuario no encontrado"));
        verify(userGameStatsRepository, never()).save(any());
    }

    @Test
    void updateUserStats_ShouldThrowException_WhenGameNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(GameNotFoundException.class, () ->
                userGameStatsService.updateUserStats(1L, 1L, 50, 1, false)
        );

        assertThat(exception.getMessage(), is("Juego no encontrado"));
        verify(userGameStatsRepository, never()).save(any());
    }


    @Test
    void getRanking_ShouldReturnTop3AndCurrentUser_WhenUserHasStats() {
        UserEntity user2 = new UserEntity();
        user2.setUsername("player2");
        UserEntity user3 = new UserEntity();
        user3.setUsername("player3");

        UserGameStatsEntity stats1 = UserGameStatsEntity.builder()
                .user(userEntity)
                .totalPoints(300)
                .build();
        UserGameStatsEntity stats2 = UserGameStatsEntity.builder()
                .user(user2)
                .totalPoints(200)
                .build();
        UserGameStatsEntity stats3 = UserGameStatsEntity.builder()
                .user(user3)
                .totalPoints(150)
                .build();

        when(userGameStatsRepository.findByGame_IdOrderByTotalPointsDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(stats1, stats2, stats3));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L))
                .thenReturn(Optional.of(statsEntity));
        when(userGameStatsRepository.countPlayersWithHigherScore(1L, 100))
                .thenReturn(3L);

        RankingResponseDTO result = userGameStatsService.getRanking(1L, 1L);

        assertThat(result.top3().size(), is(3));
        assertThat(result.top3().get(0).username(), is("maurihidalgo"));
        assertThat(result.top3().get(0).totalPoints(), is(300));
        assertThat(result.top3().get(0).position(), is(1));
        
        assertThat(result.currentUser(), is(notNullValue()));
        assertThat(result.currentUser().position(), is(4));
        assertThat(result.currentUser().totalPoints(), is(100));
        assertThat(result.currentLevel(), is(2));
    }

    
    @Test
    void getRanking_ShouldHandleNullTotalPoints_InTop3() {
        statsEntity.setTotalPoints(null);
        
        when(userGameStatsRepository.findByGame_IdOrderByTotalPointsDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(statsEntity));
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L))
                .thenReturn(Optional.of(statsEntity));
        when(userGameStatsRepository.countPlayersWithHigherScore(1L, 0))
                .thenReturn(0L);

        RankingResponseDTO result = userGameStatsService.getRanking(1L, 1L);

        assertThat(result.top3().get(0).totalPoints(), is(0));
        assertThat(result.currentUser().totalPoints(), is(0));
    }

    @Test
    void getCurrentLevel_ShouldReturnCurrentLevel_WhenStatsExist() {
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L))
                .thenReturn(Optional.of(statsEntity));

        int level = userGameStatsService.getCurrentLevel(1L, 1L);

        assertThat(level, is(equalTo(2)));
    }

    @Test
    void getCurrentLevel_ShouldReturnDefaultLevel_WhenStatsDoNotExist() {
        when(userGameStatsRepository.findByUser_IdAndGame_Id(1L, 1L))
                .thenReturn(Optional.empty());

        int level = userGameStatsService.getCurrentLevel(1L, 1L);

        assertThat(level, is(equalTo(1)));
    }
}
