package dev.sara.micos_color_code.unit;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.sara.micos_color_code.role.RoleEntity;
import dev.sara.micos_color_code.sessions.GameSessionEntity;
import dev.sara.micos_color_code.stats.UserGameStatsEntity;
import dev.sara.micos_color_code.user.UserEntity;

public class UserEntityTest {
    private UserEntity userEntity;
    private RoleEntity roleEntity;
    private UserGameStatsEntity gameStatsEntity;
    private GameSessionEntity sessionEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        roleEntity = new RoleEntity();
        gameStatsEntity = new UserGameStatsEntity();
        sessionEntity = new GameSessionEntity();
    }

    @Test
    void testNoArgsConstructor() {
        assertThat(userEntity, is(notNullValue()));
        assertThat(userEntity.getId(), is(nullValue()));
        assertThat(userEntity.getUsername(), is(nullValue()));
        assertThat(userEntity.getEmail(), is(nullValue()));
        assertThat(userEntity.getPassword(), is(nullValue()));
        assertThat(userEntity.isEnabled(), is(false));
        assertThat(userEntity.getRoles(), is(notNullValue()));
        assertThat(userEntity.getRoles(), is(empty()));
    }

    @Test
    void testAllArgsConstructor() {
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleEntity);
        
        Set<UserGameStatsEntity> gameStats = new HashSet<>();
        gameStats.add(gameStatsEntity);
        
        Set<GameSessionEntity> sessions = new HashSet<>();
        sessions.add(sessionEntity);
        
        LocalDateTime tokenDate = LocalDateTime.now();

        UserEntity user = new UserEntity(
            1L,
            "vicentabenito",
            "vicentabenito1a@gmail.com",
            "Valentinelmejor!14",
            true,
            roles,
            "token123",
            tokenDate,
            gameStats,
            sessions
        );

        assertThat(user.getId(), is(equalTo(1L)));
        assertThat(user.getUsername(), is(equalTo("vicentabenito")));
        assertThat(user.getEmail(), is(equalTo("vicentabenito1a@gmail.com")));
        assertThat(user.getPassword(), is(equalTo("Valentinelmejor!14")));
        assertThat(user.isEnabled(), is(true));
        assertThat(user.getRoles(), hasSize(1));
        assertThat(user.getConfirmationToken(), is(equalTo("token123")));
        assertThat(user.getTokenCreationDate(), is(equalTo(tokenDate)));
        assertThat(user.getGameStats(), hasSize(1));
        assertThat(user.getSessions(), hasSize(1));
    }

    @Test
    void testBuilder() {
        LocalDateTime tokenDate = LocalDateTime.now();
        
        UserEntity user = UserEntity.builder()
            .id(1L)
            .username("marisabenito")
            .email("marisabenito@gmail.com")
            .password("Chinchon123")
            .enabled(true)
            .confirmationToken("buildertoken")
            .tokenCreationDate(tokenDate)
            .build();

        assertThat(user.getId(), is(equalTo(1L)));
        assertThat(user.getUsername(), is(equalTo("marisabenito")));
        assertThat(user.getEmail(), is(equalTo("marisabenito@gmail.com")));
        assertThat(user.getPassword(), is(equalTo("Chinchon123")));
        assertThat(user.isEnabled(), is(true));
        assertThat(user.getConfirmationToken(), is(equalTo("buildertoken")));
        assertThat(user.getTokenCreationDate(), is(equalTo(tokenDate)));
    }
}
