package dev.sara.micos_color_code.sessions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/play/{gameId}/sessions")
public class GameSessionController {
    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping
    public ResponseEntity<GameSessionResponseDTO> completeGameSession( @PathVariable Long gameId, @Valid @RequestBody GameSessionRequestDTO request, Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        GameSessionResponseDTO response = gameSessionService.completeGameSession(userId, gameId, request);
        return ResponseEntity.ok(response);
    }
    
    /*private Long getUserIdFromAuthentication(Authentication authentication) {
        // Tu lógica de autenticación aquí
        return 1L; // placeholder
    } */
}
