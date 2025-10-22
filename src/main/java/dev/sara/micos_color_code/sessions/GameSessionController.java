package dev.sara.micos_color_code.sessions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sara.micos_color_code.security.SecurityUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/play/{gameId}/sessions")
public class GameSessionController {
    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping
    public ResponseEntity<GameSessionResponseDTO> completeGameSession( @PathVariable Long gameId, @Valid @RequestBody GameSessionRequestDTO request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        GameSessionResponseDTO response = gameSessionService.completeGameSession(userId, gameId, request);
        return ResponseEntity.ok(response);
    }
}
