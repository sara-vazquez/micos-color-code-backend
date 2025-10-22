package dev.sara.micos_color_code.play;

import java.util.HashSet;
import java.util.Set;

import dev.sara.micos_color_code.sessions.GameSessionEntity;
import dev.sara.micos_color_code.stats.UserGameStatsEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gameName", nullable = false, unique = true)
    private String gameName;

    @OneToMany(mappedBy="game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserGameStatsEntity> gameStats = new HashSet<>();

    @OneToMany(mappedBy="game", cascade = CascadeType.ALL)
    private Set<GameSessionEntity> sessions = new HashSet<>();
    
}
