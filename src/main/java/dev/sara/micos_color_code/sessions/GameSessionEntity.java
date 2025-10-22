package dev.sara.micos_color_code.sessions;

import dev.sara.micos_color_code.play.GameEntity;
import dev.sara.micos_color_code.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name="game_sessions", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "game_id"})})
public class GameSessionEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name="game_id", nullable=false)
    private GameEntity game;

    @Column(name="points", nullable = false)
    private int points;
    
    @Column(name="time_completed_seconds")
    private int timeCompleted;

    @Column(name="level", nullable=false)
    private int level;
}
