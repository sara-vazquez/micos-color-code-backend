package dev.sara.micos_color_code.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.sara.micos_color_code.play.GameEntity;
import dev.sara.micos_color_code.stats.UserGameStatsEntity;
import dev.sara.micos_color_code.user.UserEntity;

public class UserGameStatsEntityTest {

    private UserGameStatsEntity gameStatsEntity;
    private UserEntity userEntity;
    private GameEntity gameEntity;

    @BeforeEach
    void setUp() {
        gameStatsEntity = new UserGameStatsEntity();
        userEntity = new UserEntity();
        gameEntity = new GameEntity();
    }

    @Test
    void testNoArgsConstructor() {
        assertThat(gameStatsEntity, is(notNullValue()));
        assertThat(gameStatsEntity.getId(), is(nullValue()));
        assertThat(gameStatsEntity.getTotalPoints(), is(nullValue()));
        assertThat(gameStatsEntity.getGamesPlayed(), is(nullValue()));
        assertThat(gameStatsEntity.getCurrentLevel(), is(notNullValue()));
    }

    @Test
    void testBuilder() {
        UserGameStatsEntity stats = UserGameStatsEntity.builder()
        .id(1L)
        .totalPoints(18763)
        .gamesPlayed(12)
        .currentLevel(4)
        .build();

        assertThat(stats.getId(), is(equalTo(1L)));
        assertThat(stats.getTotalPoints(), is(equalTo(18763)));
        assertThat(stats.getGamesPlayed(), is(equalTo(12)));
        assertThat(stats.getCurrentLevel(), is(equalTo(4)));

    }
}
